/**
 * Licensed to Cloudera, Inc. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  Cloudera, Inc. licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cloudera.sqoop.mapreduce;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.ReflectionUtils;

import com.cloudera.sqoop.lib.RecordParser;
import com.cloudera.sqoop.lib.SqoopRecord;

/**
 * Mapper for the merge program which operates on text files that we need to
 * parse into SqoopRecord instances.
 */
public class MergeTextMapper extends MergeMapperBase<LongWritable, Text> {

  private SqoopRecord record;

  @Override
  protected void setup(Context c) throws IOException, InterruptedException {
    Configuration conf = c.getConfiguration();

    Class<? extends SqoopRecord> recordClass =
        (Class<? extends SqoopRecord>) conf.getClass(
        MergeJob.MERGE_SQOOP_RECORD_KEY, SqoopRecord.class);
    this.record = ReflectionUtils.newInstance(recordClass, conf);

    super.setup(c);
  }

  public void map(LongWritable key, Text val, Context c)
      throws IOException, InterruptedException {
    try {
      this.record.parse(val);
    } catch (RecordParser.ParseError pe) {
      throw new IOException(pe);
    }

    processRecord(this.record, c);
  }
}
