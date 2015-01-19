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

			Node book = db.createNode(CustomLabels.BOOK);
			book.setProperty("title", "Some book");
			Index<Node> bookIndex = indexManager.forNodes("books");
			bookIndex.add(book, "title", "Some book");

			Node author = db.createNode(CustomLabels.AUTHOR);
			author.setProperty("name", "Al Bundy");
			author.createRelationshipTo(book, CustomRelations.HAS_WRITTEN);
			Index<Node> authorIndex = indexManager.forNodes("authors");
			authorIndex.add(author, "name", "Al Bundy");

			ExecutionEngine engine = new ExecutionEngine(db);
			String cql = "START author=node:authors(name = 'Al Bundy') MATCH (author:AUTHOR)-[:HAS_WRITTEN]->(book:BOOK) RETURN author, book";
			ExecutionResult result = engine.execute(cql);
			System.out.println(result.dumpToString());
			tx.success();
		}

	}
}
