package tests;

import static org.junit.Assert.assertEquals;
import model.Link;
import model.Node;
import model.WeightedGraph;

import org.junit.Before;
import org.junit.Test;

public class WeightedGraphTest {

	Node a;
	Node b;
	Node c;
	Node d;

	Link AB;
	Link AC;
	Link BC;
	Link CD;
	Link BD;

	WeightedGraph<Node, Link> pn;

	@Before
	public void setUp() throws Exception {

		a = new Node("A", 0f, 0f);
		b = new Node("B", 0f, 3f);
		c = new Node("C", 0f, 12f);
		d = new Node("D", 3f, 8f);

		AB = new Link(a.getName() + b.getName());
		AC = new Link(a.getName() + c.getName());
		BC = new Link(b.getName() + c.getName());
		CD = new Link(c.getName() + d.getName());
		BD = new Link(b.getName() + d.getName());

		pn = new WeightedGraph<Node, Link>();
	}

	@Test
	public void testInsertion() {

		pn.insertVertex(a);
		pn.insertVertex(b);
		pn.insertVertex(c);
		pn.insertVertex(d);

		pn.insertEdge(a, b, AB);
		pn.insertEdge(b, c, BC);
		pn.insertEdge(b, d, BD);

		// Weight is set on insertion.
		assertEquals(3f, AB.getWeight(), 0);
		assertEquals(6f, BD.getWeight(), 0.2f);
	}

}
