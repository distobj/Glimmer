package com.yahoo.glimmer.indexing.generator;

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
import it.unimi.dsi.fastutil.chars.CharArraySet;
import it.unimi.dsi.fastutil.chars.CharSet;
import it.unimi.dsi.io.DelimitedWordReader;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Counters;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

import com.yahoo.glimmer.indexing.RDFDocument;
import com.yahoo.glimmer.indexing.RDFDocumentFactory.IndexType;
import com.yahoo.glimmer.indexing.ResourcesHashLoader;

public class DocumentMapperTest {
    private static final CharSet DELIMITER = new CharArraySet(Collections.singleton(' '));
    private Mockery context;
    private Mapper<LongWritable, RDFDocument, TermOccurrencePair, Occurrence>.Context mapperContext;
    private Configuration mapperConf;
    private RDFDocument doc;
    private Counters counters;
    private Map<CharSequence, Long> resourcesMap;

    @SuppressWarnings("unchecked")
    @Before
    public void before() {
	context = new Mockery();
	context.setImposteriser(ClassImposteriser.INSTANCE);
	
	mapperContext = context.mock(Context.class, "mapperContext");
	mapperConf = new Configuration();
	doc = context.mock(RDFDocument.class, "doc");
	counters = new Counters();
	
	resourcesMap = new HashMap<CharSequence, Long>();
    }
    
    @Test
    public void emptyDocTest() throws IOException, InterruptedException {
	mapperConf.setStrings("RdfFieldNames", "fieldZero");
	
	resourcesMap.put("0", 0l);
	ResourcesHashLoader.setHash(resourcesMap);
	
	context.checking(new Expectations(){{
	    allowing(mapperContext).getConfiguration();
	    will(returnValue(mapperConf));
	    
	    allowing(doc).uri();
	    will(returnValue("0"));
	    
	    one(mapperContext).getCounter(DocumentMapper.Counters.NUMBER_OF_RECORDS);
	    will(returnValue(counters.findCounter(DocumentMapper.Counters.NUMBER_OF_RECORDS)));
	    
	    allowing(doc).content(0);
	    will(returnValue(new DelimitedWordReader("".toCharArray(), DELIMITER)));
	}});
	
	
	DocumentMapper mapper = new DocumentMapper();
	mapper.setup(mapperContext);
	mapper.map(null, doc, mapperContext);
	
	context.assertIsSatisfied();
    }
    
    @Test
    public void twoFieldstest() throws IOException, InterruptedException {
	mapperConf.setStrings("RdfFieldNames", "fieldZero", "fieldOne");
	
	resourcesMap.put("4536", 10l);
	resourcesMap.put("fieldOne", 11l);
	resourcesMap.put("fieldZero", 12l);
	ResourcesHashLoader.setHash(resourcesMap);
	
	context.checking(new Expectations(){{
	    allowing(mapperContext).getConfiguration();
	    will(returnValue(mapperConf));
	    
	    allowing(mapperContext).setStatus(with(any(String.class)));
	    allowing(mapperContext).getCounter(DocumentMapper.Counters.NUMBER_OF_RECORDS);
	    will(returnValue(counters.findCounter(DocumentMapper.Counters.NUMBER_OF_RECORDS)));
	    allowing(mapperContext).getCounter(DocumentMapper.Counters.INDEXED_OCCURRENCES);
	    will(returnValue(counters.findCounter(DocumentMapper.Counters.INDEXED_OCCURRENCES)));
	    
	    allowing(doc).uri();
	    will(returnValue("4536"));
	    allowing(doc).content(0);
	    will(returnValue(new DelimitedWordReader("1 literal 3".toCharArray(), DELIMITER)));
	    allowing(doc).getIndexType();
	    will(returnValue(IndexType.VERTICAL));
	    one(mapperContext).write(with(new TermOccurrencePairMatcher(0, "1", 10, 0)), with(new OccurrenceMatcher(10, 0)));
	    one(mapperContext).write(with(new TermOccurrencePairMatcher(0, "literal", 10, 1)), with(new OccurrenceMatcher(10, 1)));
	    one(mapperContext).write(with(new TermOccurrencePairMatcher(0, "3", 10, 2)), with(new OccurrenceMatcher(10, 2)));
	    // for counting # of docs per term
	    one(mapperContext).write(with(new TermOccurrencePairMatcher(0, "1", null, 10)), with(new OccurrenceMatcher(null, 10)));
	    one(mapperContext).write(with(new TermOccurrencePairMatcher(0, "literal", null, 10)), with(new OccurrenceMatcher(null, 10)));
	    one(mapperContext).write(with(new TermOccurrencePairMatcher(0, "3", null, 10)), with(new OccurrenceMatcher(null, 10)));
	    
	    allowing(doc).content(1);
	    will(returnValue(new DelimitedWordReader("4 5".toCharArray(), DELIMITER)));
	    one(mapperContext).write(with(new TermOccurrencePairMatcher(1, "4", 10, 0)), with(new OccurrenceMatcher(10, 0)));
	    one(mapperContext).write(with(new TermOccurrencePairMatcher(1, "5", 10, 1)), with(new OccurrenceMatcher(10, 1)));
	    // for counting # of docs per term
	    one(mapperContext).write(with(new TermOccurrencePairMatcher(1, "4", null, 10)), with(new OccurrenceMatcher(null, 10)));
	    one(mapperContext).write(with(new TermOccurrencePairMatcher(1, "5", null, 10)), with(new OccurrenceMatcher(null, 10)));
	
	    // The ALIGNMENT_INDEX is created for Vertical indexes only.
	    //
	    one(mapperContext).write(with(new TermOccurrencePairMatcher(DocumentMapper.ALIGNMENT_INDEX, "1", 12, null)), with(new OccurrenceMatcher(12, null)));
	    one(mapperContext).write(with(new TermOccurrencePairMatcher(DocumentMapper.ALIGNMENT_INDEX, "literal", 12, null)), with(new OccurrenceMatcher(12, null)));
	    one(mapperContext).write(with(new TermOccurrencePairMatcher(DocumentMapper.ALIGNMENT_INDEX, "3", 12, null)), with(new OccurrenceMatcher(12, null)));
	    one(mapperContext).write(with(new TermOccurrencePairMatcher(DocumentMapper.ALIGNMENT_INDEX, "4", 11, null)), with(new OccurrenceMatcher(11, null)));
	    one(mapperContext).write(with(new TermOccurrencePairMatcher(DocumentMapper.ALIGNMENT_INDEX, "5", 11, null)), with(new OccurrenceMatcher(11, null)));
	    // 
	    one(mapperContext).write(with(new TermOccurrencePairMatcher(DocumentMapper.ALIGNMENT_INDEX, "1", null, 12)), with(new OccurrenceMatcher(null, 12)));
	    one(mapperContext).write(with(new TermOccurrencePairMatcher(DocumentMapper.ALIGNMENT_INDEX, "literal", null, 12)), with(new OccurrenceMatcher(null, 12)));
	    one(mapperContext).write(with(new TermOccurrencePairMatcher(DocumentMapper.ALIGNMENT_INDEX, "3", null, 12)), with(new OccurrenceMatcher(null, 12)));
	    one(mapperContext).write(with(new TermOccurrencePairMatcher(DocumentMapper.ALIGNMENT_INDEX, "4", null, 11)), with(new OccurrenceMatcher(null, 11)));
	    one(mapperContext).write(with(new TermOccurrencePairMatcher(DocumentMapper.ALIGNMENT_INDEX, "5", null, 11)), with(new OccurrenceMatcher(null, 11)));
	}});
	
	DocumentMapper mapper = new DocumentMapper();
	mapper.setup(mapperContext);
	mapper.map(null, doc, mapperContext);
	
	context.assertIsSatisfied();
	
	assertEquals(1l, counters.findCounter(DocumentMapper.Counters.NUMBER_OF_RECORDS).getValue());
	assertEquals(5l, counters.findCounter(DocumentMapper.Counters.INDEXED_OCCURRENCES).getValue());
    }
    
    private static class OccurrenceMatcher extends BaseMatcher<Occurrence> {
	private Occurrence occurrence;
	
	public OccurrenceMatcher(Integer document, Integer position) {
	    occurrence = new Occurrence(document, position);
	}
	
	@Override
	public boolean matches(Object object) {
	    return occurrence.equals(object);
	}
	
	@Override
	public void describeTo(Description description) {
	    description.appendText(occurrence.toString());
	}
    }
}