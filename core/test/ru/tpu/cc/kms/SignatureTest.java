/**
 *
 */
package ru.tpu.cc.kms;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertArrayEquals;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class SignatureTest {

	/**
	 * Test method for {@link ru.tpu.cc.kms.Signature#Signature(java.util.Set)}.
	 * @throws URISyntaxException
	 */
	@Test
	public final void testSignatureSetOfURI() throws URISyntaxException {
		Set<URI> set1 = new HashSet<URI>();
		set1.add(new URI("http://yandex.ru/"));
		set1.add(new URI("http://kms.cc.tpu.ru/ontologies/core.owl#Person"));
		Signature sig = new Signature(set1);
		assertTrue(sig.containsAll(set1));
	}

	/**
	 * Test method for {@link ru.tpu.cc.kms.Signature#Signature(java.net.URI)}.
	 * @throws URISyntaxException
	 */
	@Test
	public final void testSignatureURI() throws URISyntaxException {
		URI uri = new URI("http://yandex.ru/");
		Signature sig = new Signature(uri);
		assertArrayEquals(new URI[]{uri}, sig.toArray());
	}
}
