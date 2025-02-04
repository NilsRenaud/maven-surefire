package org.apache.maven.surefire.api.booter;

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
import org.apache.maven.surefire.api.report.StackTraceWriter;
import org.apache.maven.surefire.api.report.TestOutputReportEntry;

import java.util.Map;

/**
 * An abstraction for physical encoder of events.
 *
 * @author <a href="mailto:tibordigana@apache.org">Tibor Digana (tibor17)</a>
 * @since 3.0.0-M5
 */
public interface MasterProcessChannelEncoder
{
    boolean checkError();

    void onJvmExit();

    void systemProperties( Map<String, String> sysProps );

    void testSetStarting( ReportEntry reportEntry, boolean trimStackTraces );

    void testSetCompleted( ReportEntry reportEntry, boolean trimStackTraces );

    void testStarting( ReportEntry reportEntry, boolean trimStackTraces );

    void testSucceeded( ReportEntry reportEntry, boolean trimStackTraces );

    void testFailed( ReportEntry reportEntry, boolean trimStackTraces );

    void testSkipped( ReportEntry reportEntry, boolean trimStackTraces );

    void testError( ReportEntry reportEntry, boolean trimStackTraces );

    void testAssumptionFailure( ReportEntry reportEntry, boolean trimStackTraces );

    void testOutput( TestOutputReportEntry reportEntry );

    void consoleInfoLog( String msg );

    void consoleErrorLog( String msg );

    void consoleErrorLog( Throwable t );

    void consoleErrorLog( String msg, Throwable t );

    void consoleErrorLog( StackTraceWriter stackTraceWriter, boolean trimStackTraces );

    void consoleDebugLog( String msg );

    void consoleWarningLog( String msg );

    void bye();

    void stopOnNextTest();

    void acquireNextTest();

    void sendExitError( StackTraceWriter stackTraceWriter, boolean trimStackTraces );
}
