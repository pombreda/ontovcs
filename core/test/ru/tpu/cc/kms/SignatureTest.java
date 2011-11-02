package ru.tpu.cc.kms;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertArrayEquals;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class SignatureTest {

	private List<URI> uris;

    @Before
    public void setUp() throws URISyntaxException {
        uris = new ArrayList<URI>();
        uris.add(new URI("http://www.w3.org/XML/1998/namespace"));
        uris.add(new URI("http://www.w3.org/1999/02/22-rdf-syntax-ns#"));
        uris.add(new URI("http://www.w3.org/2000/01/rdf-schema#"));
        uris.add(new URI("http://www.w3.org/2001/XMLSchema#"));
        uris.add(new URI("http://www.w3.org/2002/07/owl#"));
        uris.add(new URI("http://www.w3.org/2006/12/owl11#"));
        uris.add(new URI("http://www.w3.org/2006/12/owl11-xml#"));
    }

	/**
	 * Test method for {@link ru.tpu.cc.kms.Signature#Signature(java.util.Set)}.
	 */
	@Test
	public final void testSignatureSetOfURI() {
		Set<URI> set1 = new HashSet<URI>();
		set1.addAll(uris);
		Signature sig = new Signature(set1);
		assertTrue(sig.containsAll(set1));
	}

	/**
	 * Test method for {@link ru.tpu.cc.kms.Signature#Signature(java.net.URI)}.
	 */
	@Test
	public final void testSignatureURI() {
		Signature sig = new Signature(uris.get(0));
		assertArrayEquals(new URI[]{uris.get(0)}, sig.toArray());
	}

	/**
	 * Test method for {@link ru.tpu.cc.kms.Signature#isIntersectionEmpty(Signature)}.
	 */
	@Test
	public final void test_isIntersectionEmpty() {
		Signature sig1 = new Signature(new HashSet<URI>(uris.subList(0, uris.size() / 2 - 1)));
		Signature sig2 = new Signature(new HashSet<URI>(uris.subList(uris.size() / 2, uris.size())));
		assert(sig1.isIntersectionEmpty(sig2));
		sig1.add(uris.get(uris.size() - 1));
		assert(!sig1.isIntersectionEmpty(sig2));
	}

	/**
	 * Test method for {@link ru.tpu.cc.kms.Signature#getIntersectionWith(Signature)}.
	 */
	@Test
	public final void test_getIntersectionWith() {
		Signature sig1 = new Signature(new HashSet<URI>(uris.subList(0, uris.size() / 2 - 1)));
		Signature sig2 = new Signature(new HashSet<URI>(uris.subList(uris.size() / 2, uris.size())));
		assertArrayEquals(sig1.getIntersectionWith(sig2).toArray(), new URI[]{});
		sig1.add(uris.get(uris.size() - 1));
		assertArrayEquals(sig1.getIntersectionWith(sig2).toArray(), new URI[]{uris.get(uris.size() - 1)});
	}
}
