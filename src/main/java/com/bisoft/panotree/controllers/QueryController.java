package com.bisoft.panotree.controllers;

import com.bisoft.navi.common.exceptions.LoadConnectionParameterException;
import com.bisoft.navi.common.exceptions.LoadResourceException;
import com.bisoft.navi.common.resources.FilesResource;
import com.bisoft.navi.common.resources.MapResource;
import com.bisoft.navi.common.resources.XMLResource;
import com.bisoft.panotree.interfaces.IOpenedConnection;
import com.bisoft.panotree.models.DBConnection;
import com.bisoft.panotree.models.NaviNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.driver.Record;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.min;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping(method=GET)
public class QueryController {

	DBConnection db = new DBConnection(new MapResource("db.properties").loadedResource());
	Map<String, String> queries = new XMLResource(
			com.bisoft.navi.App.class.getClassLoader().getResourceAsStream("cql_collection.xml")
	).loadedResource();

	private final Map<String, String> i18n = new HashMap<>();
	{
		i18n.put("well", "Скважины");
		i18n.put("bush", "Кусты");
		i18n.put("devobject", "Промобъекты");
		i18n.put("stratum", "Пласты");
		i18n.put("cdng", "ЦДНГ");
		i18n.put("ns", "НС");
		i18n.put("oilfield", "Месторождения");
		i18n.put("contour", "Контура");
	}
	
	public QueryController() throws LoadResourceException, LoadConnectionParameterException {
	}
	
	@CrossOrigin(origins = "*")
	@Cacheable
	@GetMapping("/content")
	public Object getPath(@RequestParam("node-path") String obj, @RequestParam("start") int startIndex, @RequestParam("count") int countElement) throws Exception {
		//  Create/load a map to hold the parameter
		String distinctMode = "label";
		String cypher = "";
		String cypher_count = "";
		Map<String, Object> params = new HashMap<>(5);
		params.put ("start", startIndex);
		params.put ("count", countElement);
		NaviNode[] nodes = new NaviNode[0];

		// Prepare cypher query
		ObjectMapper mapper = new ObjectMapper();
		try {
			nodes = mapper.readValue(obj, NaviNode[].class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new ResponseStatusException(
				HttpStatus.BAD_REQUEST, "object string incorrect", e);
		}
		NaviNode node = nodes[nodes.length - 1];
		
		if(node.getMnem().equals("root")) {
			cypher =  queries.get("get_top_nodes");
			cypher_count =  queries.get("get_top_nodes_count");
		}else{
			params.put ("id", node.getId());
			//params.put ("names", new String[] {"well","bush","cdng","contour","ns","devobject","stratum"});
			params.put ("path", Arrays.stream(nodes)
					.map(n -> n.getMnem())
					.toArray());
			params.put ("pathNames", "name".equals(distinctMode) ? Arrays.stream(nodes)
					.map(n -> n.getName()).toArray(): new String[0]);
			params.put ("label", node.getMnem());
			if (node.getOtype().equals("folder")) {
				cypher = queries.get("get_folder_children");
				cypher_count = queries.get("get_folder_children_count");
				params.put ("id", Integer.valueOf(node.getPid()));
			} else {
				cypher = String.format(queries.get("get_node_children"), node.getMnem());
				cypher_count = String.format(queries.get("get_node_children_count"), node.getMnem());
//				params.put ("id", node.getId());
//				params.put ("mnem", node.getMnem());

//				cypher = String.format("MATCH (root:%1$s {uid:%2$s})<--(n) " +
//					"unwind [l in labels(n) where (l in $names) AND not (l = $path) | l ] as foo WITH " +
//					"{mnem:[foo], name:foo, id: 0, pid: %2$s} as obj " +
//					"RETURN obj as foo " +
//					"UNION MATCH (root {uid:%2$s})<--(n) WHERE all(l in labels(n) where not (l in $names) AND not (l = $path)) AND not (n.name in $pathNames) " +
//					"WITH {mnem:labels(n), name:n.name, id:n.uid, pid: %2$s} as obj " +
//					"RETURN obj as foo", node.getMnem(), node.getId());
			}
		}

		List<NaviNode> target;
		int rowCounts = 0;
		try (IOpenedConnection dbConnection = db.openedConnection())
		{
			String finalCypher = cypher;
			List<Record> result = dbConnection.session().readTransaction (
					tx -> tx.run(finalCypher, params).list()
			);
			target = result.stream()
					//.map(v -> v.get("foo").asMap())
					.map(v -> new NaviNode(
							v.get("id").asInt(),
							v.get("mnem").asString(),
							v.get("name").asString(),
							v.get("pid").asInt(),
							v.get("otype").asString(),
							v.get("cnt").asInt()
					))
					.collect(Collectors.toList());
			String finalCypher2 = cypher_count;
			result = dbConnection.session().readTransaction (
					tx -> tx.run(finalCypher2, params).list()
			);
			rowCounts = result
					.stream()
					.map(v -> Map.of( "count_rows", v.get("count_rows").asInt()))
					.findFirst()
					.orElse(Map.of("count_rows", 0))
					.get("count_rows");
		}

//		Iterable<Object> res = session.query(Object.class, cypher, params);// RestPreconditions.checkFound(service.getRoot());
//		ArrayList<NaviNode> target = getNaviNodes1(res);

//		if(nodes != null) {
//			for(int i=0; i < nodes.length-2; i++) {
//				if(nodes[i].getId().length() > 0) {
//					params.put ("id", nodes[i].getId());
//					cypher = "MATCH (po: {uid:$id})<-[:CONTAINED_INTO]-(o:" + params.get("label") + ") " +
//						"WITH {mnem:labels(o), name:o.name, id: o.uid, pid: $id} as obj " +
//						"RETURN obj";
//					res = session.query(Object.class, cypher, params);// RestPreconditions.checkFound(service.getRoot());
//					ArrayList<NaviNode> parentNodes = getNaviNodes1(res);
//					if(parentNodes.size() > 0){
//						target = innerJoin(parentNodes, target);
//					}
//				}
//			}
//		}

		//target.forEach(v -> v.setName() = i18n.containsKey(v.getName()) ? i18n.get(v.getName()) : v.getName());
		//ArrayList<Object> list = new ArrayList<>();
		//if (target.size() > 0) {
		//int maxElements = min(rowCounts, startIndex + countElement);
		// list = new ArrayList(target); // .subList(startIndex, maxElements)
		//}
		Object res = new PageResponce(target, rowCounts);
		return res;
	}
	
}
