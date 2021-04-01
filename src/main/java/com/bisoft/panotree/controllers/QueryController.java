package com.bisoft.panotree.controllers;

import com.bisoft.panotree.models.NaviNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.driver.Record;
import org.neo4j.driver.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.min;

@RestController
@RequestMapping()
public class QueryController {
	static private final String NEO_URI = "bolt://localhost:7687";
	static private final String NEO_USERNAME = "neo4j";
	static private final String NEO_PASSWORD = "admin";
	Driver driver = GraphDatabase.driver( NEO_URI, AuthTokens.basic( NEO_USERNAME, NEO_PASSWORD ) );
	
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
	
	@CrossOrigin(origins = "*")
	@Cacheable
	@GetMapping("/content")
	public Object getPath(@RequestParam("object") String obj, @RequestParam("start") int startIndex, @RequestParam("count") int countElement) {
		//  Create/load a map to hold the parameter
		String distinctMode = "label";
		String cypher = "";
		Map<String, Object> params = new HashMap<>(5);
		NaviNode[] nodes = null;
		
		// Prepare cypher query
		if(obj.length() == 0) {
			cypher = "MATCH (c:oilfield) WITH {mnem:labels(c), name:c.name, id: c.uid, pid: ''} as foo RETURN foo";
		}else{
			ObjectMapper mapper = new ObjectMapper();
			try {
				nodes = mapper.readValue(obj, NaviNode[].class);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			NaviNode node = nodes[nodes.length - 1];
			params.put ("id", Integer.valueOf(node.getId()));
			params.put ("names", new String[] {"well","bush","cdng","contour","ns","devobject","stratum"});
			params.put ("path", node.getMnem());
			params.put ("pathNames", "name".equals(distinctMode) ? Arrays.stream(nodes)
				.filter(v -> v.getId().length() > 0)
				.map(n -> n.getName()).toArray(): new String[0]);
			params.put ("label", node.getMnem());
			if (node.getId() == null) {
				params.put ("id", Integer.valueOf(node.getPid()));
				cypher = "MATCH (po: {uid:$id})<-[:CONTAINED_INTO]-(o:" + params.get("label") + ") " +
					"WHERE all(l in labels(o) where not (l = $path)) AND not (o.name in $pathNames)" +
					"WITH {mnem:labels(o), name:o.name, id: o.uid, pid: $id} as foo " +
					"RETURN foo";
			} else {
				cypher = String.format("MATCH (root:%1$s {uid:%2$s})<--(n) " +
					"unwind [l in labels(n) where l in $names AND not (l = $path) | l ] as foo WITH " +
					"{mnem:[foo], name:foo, id: 0, pid: %2$s} as obj " +
					"RETURN obj as foo " +
					"UNION MATCH (root {uid:%2$s})<--(n) WHERE all(l in labels(n) where not (l in $names) AND not (l = $path)) AND not (n.name in $pathNames) " +
					"WITH {mnem:labels(n), name:n.name, id:n.uid, pid: %2$s} as obj " +
					"RETURN obj as foo", node.getMnem(), node.getId());
			}
		}
		
		List<NaviNode> target;
		try ( Session session = driver.session() )
		{
			String finalCypher = cypher;
			List<Record> result = session.readTransaction (
				tx -> tx.run(finalCypher, params).list()
			);
			target = result.stream()
				.map(v -> v.get("foo").asMap())
				.map(v -> new NaviNode(
					v.get("id").toString(),
					v.get("mnem").toString(),
					v.get("name").toString(),
					v.get("pid").toString()
				))
			.collect(Collectors.toList());
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
		int maxElements = min(target.size()-1, startIndex + countElement);
		ArrayList<Object> list = new ArrayList(target.subList(startIndex, maxElements));
		Object res = new PageResponce(list, target.size());
		return res;
	}
	
}
