/**
 * Copyright (C) 2018 Kurt Raschke <kurt@kurtraschke.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.gtfs_transformer.updates;

import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class ScriptTransformStrategy implements GtfsTransformStrategy {

    private final Logger LOG = LoggerFactory.getLogger(ScriptTransformStrategy.class);

    @CsvField(name="engineShortName", optional = true)
    private String engineShortName = "nashorn";

    @CsvField(name="scriptContents", optional = true)
    private String scriptContents;

    @CsvField(name="scriptFile", optional = true)
    private String scriptFile;

    @CsvField(name="methodName", optional = true)
    private String methodName = "run";

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {
        ScriptEngine engine = (new ScriptEngineManager()).getEngineByName(engineShortName);

        try {
            if ((scriptContents == null) == (scriptFile == null)) {
                throw new IllegalArgumentException("One and only one of scriptContents or scriptFile must be specified.");
            } else if (scriptContents != null) {
                engine.eval(scriptContents);
            } else {
                try (Reader fr = new FileReader(scriptFile)) {
                    engine.eval(fr);
                }
            }

            ((Invocable) engine).invokeFunction(methodName, context, dao);
        } catch (ScriptException | NoSuchMethodException | IOException e) {
            LOG.error("Error while invoking script", e);
            throw new RuntimeException(e);
        }

    }

    public void setEngineShortName(String engineShortName) {
        this.engineShortName = engineShortName;
    }

    public void setScriptContents(String scriptContents) {
        this.scriptContents = scriptContents;
    }

    public void setScriptFile(String scriptFilename) {
        this.scriptFile = scriptFilename;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
}
