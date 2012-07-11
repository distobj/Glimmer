package com.yahoo.glimmer.indexing;

/*
 * Copyright (c) 2012 Yahoo! Inc. All rights reserved.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is 
 *  distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and limitations under the License.
 *  See accompanying LICENSE file.
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import it.unimi.dsi.lang.MutableString;

import java.io.IOException;

import org.junit.Test;

public class HorizontalDocumentFactoryTest extends AbstractDocumentFactoryTest {
    
    @Test
    public void withContextTest() throws IOException {
	HorizontalDocumentFactory.setupConf(conf, true);
	
	resourcesMap.put("http://context/1", 55l);
	resourcesMap.put("http://object/1", 45l);
	resourcesMap.put("http://object/2", 46l);
	ResourcesHashLoader.setHash(resourcesMap);
	
	HorizontalDocumentFactory factory = (HorizontalDocumentFactory)RDFDocumentFactory.buildFactory(conf);
	assertEquals(4, factory.numberOfFields());
	HorizontalDocument document = (HorizontalDocument)factory.getDocument();
	document.setContent(rawContentInputStream);
	
	assertEquals("http://subject/", document.uri());
	
	MutableString word = new MutableString();
	MutableString nonWord = new MutableString();
	
	// token, predicate & context are positional/parallel indexes.
	WordArrayReader tokenReader = (WordArrayReader)document.content(0);
	WordArrayReader predicateReader = (WordArrayReader)document.content(1);
	WordArrayReader contextReader = (WordArrayReader)document.content(2);
	
	assertTrue(tokenReader.next(word, nonWord));
	assertEquals("45", word.toString());
	assertEquals("", nonWord.toString());
	assertTrue(predicateReader.next(word, nonWord));
	assertEquals("http_predicate_1", word.toString());
	assertEquals("", nonWord.toString());
	assertTrue(contextReader.next(word, nonWord));
	assertEquals(RDFDocument.NULL_URL, word.toString());
	assertEquals("", nonWord.toString());
	
	assertTrue(tokenReader.next(word, nonWord));
	assertEquals("object", word.toString());
	assertEquals("", nonWord.toString());
	assertTrue(predicateReader.next(word, nonWord));
	assertEquals("http_predicate_3", word.toString());
	assertEquals("", nonWord.toString());
	assertTrue(contextReader.next(word, nonWord));
	assertEquals("55", word.toString());
	assertEquals("", nonWord.toString());
	
	assertTrue(tokenReader.next(word, nonWord));
	assertEquals("3", word.toString());
	assertEquals("", nonWord.toString());
	assertTrue(predicateReader.next(word, nonWord));
	assertEquals("http_predicate_3", word.toString());
	assertEquals("", nonWord.toString());
	assertTrue(contextReader.next(word, nonWord));
	assertEquals("55", word.toString());
	assertEquals("", nonWord.toString());
	
	assertTrue(tokenReader.next(word, nonWord));
	assertEquals("46", word.toString());
	assertEquals("", nonWord.toString());
	assertTrue(predicateReader.next(word, nonWord));
	assertEquals("http_predicate_2", word.toString());
	assertEquals("", nonWord.toString());
	assertTrue(contextReader.next(word, nonWord));
	assertEquals(RDFDocument.NULL_URL, word.toString());
	assertEquals("", nonWord.toString());
	
	assertFalse(tokenReader.next(word, nonWord));
	assertFalse(predicateReader.next(word, nonWord));
	assertFalse(contextReader.next(word, nonWord));
	
	WordArrayReader uriReader = (WordArrayReader)document.content(3);
	assertTrue(uriReader.next(word, nonWord));
	assertEquals("subject", word.toString());
	assertEquals("", nonWord.toString());
	assertFalse(uriReader.next(word, nonWord));
	
	context.assertIsSatisfied();
	
	assertEquals(3l, factory.getCounters().findCounter(RDFDocumentFactory.RdfCounters.INDEXED_TRIPLES).getValue());
    }
    
    @Test
    public void withoutContextTest() throws IOException {
	HorizontalDocumentFactory.setupConf(conf, false);
	
	resourcesMap.put("http://context/1", 55l);
	resourcesMap.put("http://object/1", 45l);
	resourcesMap.put("http://object/2", 46l);
	ResourcesHashLoader.setHash(resourcesMap);
	
	HorizontalDocumentFactory factory = (HorizontalDocumentFactory) RDFDocumentFactory.buildFactory(conf);
	assertEquals(4, factory.numberOfFields());
	HorizontalDocument document = (HorizontalDocument)factory.getDocument();
	document.setContent(rawContentInputStream);
	assertEquals("http://subject/", document.uri());
	
	MutableString word = new MutableString();
	MutableString nonWord = new MutableString();
	
	// token, predicate & context are positional/parallel indexes.
	WordArrayReader tokenReader = (WordArrayReader)document.content(0);
	WordArrayReader predicateReader = (WordArrayReader)document.content(1);
	WordArrayReader contextReader = (WordArrayReader)document.content(2);
	
	assertTrue(tokenReader.next(word, nonWord));
	assertEquals("45", word.toString());
	assertEquals("", nonWord.toString());
	assertTrue(predicateReader.next(word, nonWord));
	assertEquals("http_predicate_1", word.toString());
	assertEquals("", nonWord.toString());
	assertTrue(contextReader.next(word, nonWord));
	assertEquals(RDFDocument.NULL_URL, word.toString());
	assertEquals("", nonWord.toString());
	
	assertTrue(tokenReader.next(word, nonWord));
	assertEquals("object", word.toString());
	assertEquals("", nonWord.toString());
	assertTrue(predicateReader.next(word, nonWord));
	assertEquals("http_predicate_3", word.toString());
	assertEquals("", nonWord.toString());
	assertTrue(contextReader.next(word, nonWord));
	assertEquals(RDFDocument.NULL_URL, word.toString());
	assertEquals("", nonWord.toString());
	
	assertTrue(tokenReader.next(word, nonWord));
	assertEquals("3", word.toString());
	assertEquals("", nonWord.toString());
	assertTrue(predicateReader.next(word, nonWord));
	assertEquals("http_predicate_3", word.toString());
	assertEquals("", nonWord.toString());
	assertTrue(contextReader.next(word, nonWord));
	assertEquals(RDFDocument.NULL_URL, word.toString());
	assertEquals("", nonWord.toString());
	
	assertTrue(tokenReader.next(word, nonWord));
	assertEquals("46", word.toString());
	assertEquals("", nonWord.toString());
	assertTrue(predicateReader.next(word, nonWord));
	assertEquals("http_predicate_2", word.toString());
	assertEquals("", nonWord.toString());
	assertTrue(contextReader.next(word, nonWord));
	assertEquals(RDFDocument.NULL_URL, word.toString());
	assertEquals("", nonWord.toString());
	
	assertFalse(tokenReader.next(word, nonWord));
	assertFalse(predicateReader.next(word, nonWord));
	assertFalse(contextReader.next(word, nonWord));
	
	WordArrayReader uriReader = (WordArrayReader)document.content(3);
	assertTrue(uriReader.next(word, nonWord));
	assertEquals("subject", word.toString());
	assertEquals("", nonWord.toString());
	assertFalse(uriReader.next(word, nonWord));
	
	context.assertIsSatisfied();
	
	assertEquals(3l, factory.getCounters().findCounter(RDFDocumentFactory.RdfCounters.INDEXED_TRIPLES).getValue());
    }
}