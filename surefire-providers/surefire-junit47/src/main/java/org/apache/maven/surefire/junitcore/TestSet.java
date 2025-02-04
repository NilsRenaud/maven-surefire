package org.apache.maven.surefire.junitcore;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.surefire.api.report.ReportEntry;
import org.apache.maven.surefire.api.report.SimpleReportEntry;
import org.apache.maven.surefire.api.report.TestReportListener;
import org.apache.maven.surefire.api.report.TestSetReportEntry;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.maven.surefire.api.util.internal.ObjectUtils.systemProps;

/**
 * * Represents the test-state of a testset that is run.
 */
public class TestSet
{
    private static final InheritableThreadLocal<TestSet> TEST_SET = new InheritableThreadLocal<>();

    private final String testClassName;

    private final Collection<TestMethod> testMethods = new ConcurrentLinkedQueue<>();

    private final AtomicBoolean played = new AtomicBoolean();

    private final AtomicInteger numberOfCompletedChildren = new AtomicInteger();

    // While the two parameters may seem duplicated, it is not entirely the case,
    // since numberOfTests has the correct value from the start, while testMethods grows as method execution starts.

    private final AtomicInteger numberOfTests = new AtomicInteger();

    private volatile boolean allScheduled;

    public TestSet( String testClassName )
    {
        this.testClassName = testClassName;
    }

    public void replay( TestReportListener target )
    {
        if ( played.compareAndSet( false, true ) )
        {
            try
            {
                TestSetReportEntry report = createReportEntryStarted();

                target.testSetStarting( report );

                long startTime = 0;
                long endTime = 0;
                for ( TestMethod testMethod : testMethods )
                {
                    if ( startTime == 0 || testMethod.getStartTime() < startTime )
                    {
                        startTime = testMethod.getStartTime();
                    }

                    if ( endTime == 0 || testMethod.getEndTime() > endTime )
                    {
                        endTime = testMethod.getEndTime();
                    }

                    testMethod.replay( target );
                }

                int elapsed = (int) ( endTime - startTime );

                report = createReportEntryCompleted( elapsed );

                target.testSetCompleted( report );
            }
            catch ( Exception e )
            {
                throw new RuntimeException( e );
            }
        }
    }

    public TestMethod createThreadAttachedTestMethod( ReportEntry description )
    {
        TestMethod testMethod = new TestMethod( description, this );
        addTestMethod( testMethod );
        testMethod.attachToThread();
        return testMethod;
    }

    private TestSetReportEntry createReportEntryStarted()
    {
        return createReportEntry( null, Collections.<String, String>emptyMap() );
    }

    private TestSetReportEntry createReportEntryCompleted( int elapsed )
    {
        return createReportEntry( elapsed, systemProps() );
    }

    private TestSetReportEntry createReportEntry( Integer elapsed, Map<String, String> systemProps )
    {
        return new SimpleReportEntry( testClassName, null, testClassName, null, null, elapsed, systemProps );
    }

    public void incrementTestMethodCount()
    {
        numberOfTests.incrementAndGet();
    }

    private void addTestMethod( TestMethod testMethod )
    {
        testMethods.add( testMethod );
    }

    public void incrementFinishedTests( TestReportListener reporterManager, boolean reportImmediately )
    {
        numberOfCompletedChildren.incrementAndGet();
        if ( allScheduled && isAllTestsDone() && reportImmediately )
        {
            replay( reporterManager );
        }
    }

    public void setAllScheduled( TestReportListener reporterManager )
    {
        allScheduled = true;
        if ( isAllTestsDone() )
        {
            replay( reporterManager );
        }
    }

    private boolean isAllTestsDone()
    {
        return numberOfTests.get() == numberOfCompletedChildren.get();
    }

    public void attachToThread()
    {
        TEST_SET.set( this );
    }

    public static TestSet getThreadTestSet()
    {
        return TEST_SET.get();
    }
}
