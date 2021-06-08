MATCH (o) WHERE NOT EXISTS { MATCH (o)-[:CONTAINED_INTO]->(n) }
WITH {mnem:labels(o)[0], name:o.name, id: o.uid, pid: '', otype: 'item', oname: labels(o)[0], cnt: 0} as foo
return foo.mnem as mnem, foo.id as id, foo.otype as otype, foo.name as name, foo.cnt as cnt, foo.oname as oname, foo.pid as pid;