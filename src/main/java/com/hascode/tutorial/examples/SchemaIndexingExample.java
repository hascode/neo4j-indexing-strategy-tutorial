package com.hascode.tutorial.examples;

import java.io.IOException;
import java.nio.file.Files;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import com.hascode.tutorial.label.CustomLabels;
import com.hascode.tutorial.relation.CustomRelations;

public class SchemaIndexingExample {
	public static void main(final String[] args) throws IOException {
		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(Files.createTempDirectory("graphdb-").toString());
		try (Transaction tx = db.beginTx()) {
			db.schema().indexFor(CustomLabels.AUTHOR).on("name").create();
			db.schema().indexFor(CustomLabels.BOOK).on("title").create();
			tx.success();
		}

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
			tx.success();
		}

		try (Transaction tx = db.beginTx()) {
			ExecutionEngine engine = new ExecutionEngine(db);

			// query for books written by Al Bundy (using a query hint for index
			// selection)
			String cql1 = "MATCH (author:AUTHOR)-[:HAS_WRITTEN]->(book:BOOK) USING INDEX author:AUTHOR(name) WHERE author.name='Al Bundy' RETURN author, book";
			ExecutionResult result1 = engine.execute(cql1);
			System.out.println(result1.dumpToString());

			// query for books written by Peggy Bundy (using a query hint for
			// index selection)
			String cql2 = "MATCH (author:AUTHOR)-[:HAS_WRITTEN]->(book:BOOK) USING INDEX author:AUTHOR(name) WHERE author.name='Peggy Bundy' RETURN author, book";
			ExecutionResult result2 = engine.execute(cql2);
			System.out.println(result2.dumpToString());
			tx.success();
		}

	}
}
