package com.hascode.tutorial.examples;

import java.io.IOException;
import java.nio.file.Files;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;

import com.hascode.tutorial.label.CustomLabels;
import com.hascode.tutorial.relation.CustomRelations;

public class ManualIndexingExample {
	public static void main(final String[] args) throws IOException {
		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(Files.createTempDirectory("graphdb-").toString());
		try (Transaction tx = db.beginTx()) {
			IndexManager indexManager = db.index();

			Node book1 = db.createNode(CustomLabels.BOOK);
			book1.setProperty("title", "Some book");
			Node book2 = db.createNode(CustomLabels.BOOK);
			book2.setProperty("title", "Another book");

			Index<Node> bookIndex = indexManager.forNodes("books");
			bookIndex.add(book1, "title", "Some book");
			bookIndex.add(book2, "title", "Another book");

			Node author1 = db.createNode(CustomLabels.AUTHOR);
			author1.setProperty("name", "Al Bundy");
			Node author2 = db.createNode(CustomLabels.AUTHOR);
			author2.setProperty("name", "Peggy Bundy");

			Index<Node> authorIndex = indexManager.forNodes("authors");
			authorIndex.add(author1, "name", "Al Bundy");
			authorIndex.add(author2, "name", "Peggy Bundy");

			author1.createRelationshipTo(book1, CustomRelations.HAS_WRITTEN);
			author2.createRelationshipTo(book2, CustomRelations.HAS_WRITTEN);

			ExecutionEngine engine = new ExecutionEngine(db);
			// query for books written by Al Bundy
			String cql1 = "START author=node:authors(name = 'Al Bundy') MATCH (author)-[:HAS_WRITTEN]->(book:BOOK) RETURN author, book";
			ExecutionResult result1 = engine.execute(cql1);
			System.out.println(result1.dumpToString());

			// query for books written by Peggy Bundy
			String cql2 = "START author=node:authors(name = 'Peggy Bundy') MATCH (author)-[:HAS_WRITTEN]->(book:BOOK) RETURN author, book";
			ExecutionResult result2 = engine.execute(cql2);
			System.out.println(result2.dumpToString());
			tx.success();
		}

	}
}
