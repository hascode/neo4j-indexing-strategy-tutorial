package com.hascode.tutorial.examples;

import java.io.IOException;
import java.nio.file.Files;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.graphdb.index.AutoIndexer;
import org.neo4j.graphdb.index.IndexHits;

import com.hascode.tutorial.label.CustomLabels;
import com.hascode.tutorial.relation.CustomRelations;

public class AutoIndexingExample {
	public static void main(final String[] args) throws IOException {
		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(Files.createTempDirectory("graphdb-").toString())
				.setConfig(GraphDatabaseSettings.node_keys_indexable, "name, title").setConfig(GraphDatabaseSettings.node_auto_indexing, "true").newGraphDatabase();
		try (Transaction tx = db.beginTx()) {
			Node book1 = db.createNode(CustomLabels.BOOK);
			book1.setProperty("title", "Some book");

			Node book2 = db.createNode(CustomLabels.BOOK);
			book2.setProperty("title", "Another book");

			Node author1 = db.createNode(CustomLabels.AUTHOR);
			author1.setProperty("name", "Al Bundy");
			author1.createRelationshipTo(book1, CustomRelations.HAS_WRITTEN);

			Node author2 = db.createNode(CustomLabels.AUTHOR);
			author2.setProperty("name", "Peggy Bundy");
			author2.createRelationshipTo(book2, CustomRelations.HAS_WRITTEN);

			ExecutionEngine engine = new ExecutionEngine(db);

			// query for books written by Al Bundy
			String cql1 = "START author=node:node_auto_index(name='Al Bundy') MATCH (author)-[:HAS_WRITTEN]->(book) RETURN author, book";
			ExecutionResult result1 = engine.execute(cql1);
			System.out.println(result1.dumpToString());

			// query for books written by Peggy Bundy
			String cql2 = "START author=node:node_auto_index(name='Peggy Bundy') MATCH (author)-[:HAS_WRITTEN]->(book) RETURN author, book";
			ExecutionResult result2 = engine.execute(cql2);
			System.out.println(result2.dumpToString());
			tx.success();

			AutoIndexer<Node> autoIndexer = db.index().getNodeAutoIndexer();
			IndexHits<Node> alBundy = autoIndexer.getAutoIndex().get("name", "Al Bundy");
			System.out.println("search from auto indexer returned: " + alBundy.getSingle().getProperty("name"));
		}

	}
}
