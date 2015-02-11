/**
 * Copyright 2015 ArcBees Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.arcbees.gwtp.upgrader;

import java.awt.Label;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.JProgressBar;

public class SlotCollector {

    private final static Logger LOGGER = Logger.getGlobal();
    
    private final Set<String> absolutePaths = new HashSet<>();

    public SlotCollector(File rootDir, boolean upgrade, JProgressBar progressBar, Label progressLabel) {
        
        progressBar.setMaximum(100);
        progressBar.setValue(10);
        
        progressLabel.setText("Searching for Presenter classes");
        PresenterCollector presenterCollector = new PresenterCollector();
        processDir(rootDir, presenterCollector);
        
        progressBar.setValue(40);
        Set<String> allPresenters = presenterCollector.getPresenters();
        LOGGER.info("Found the following Presenter Classes:\n\n" + allPresenters);
        
        progressLabel.setText("Rewriting Content Slots");
        ContentSlotRewriter contentSlotRewriter = new ContentSlotRewriter(allPresenters, upgrade);
        processDir(rootDir, contentSlotRewriter);
        progressBar.setValue(80);
        
        contentSlotRewriter.startSecondRun();
        processDir(rootDir, contentSlotRewriter);
        progressBar.setValue(90);
        
        
        progressLabel.setText("Rewriting Object Slots");
        ObjectSlotRewriter objectSlotRewriter = new ObjectSlotRewriter(contentSlotRewriter.getSlotNames(), upgrade);
        processDir(rootDir, objectSlotRewriter);
        progressBar.setValue(100);
        progressLabel.setText("Finished converting Project to GWTP " + (upgrade ? "2" : "1") + ".x");
    }

    private void processDir(File dir, AbstractReWriter cuProcessor) {
        processDir(dir, cuProcessor, true);
    }
    
    private void processDir(File dir, AbstractReWriter cuProcessor, boolean reset) {
        if (reset) {
            absolutePaths.clear();
        }
        if (absolutePaths.add(dir.getAbsolutePath())) {
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    processDir(file, cuProcessor, false);
                } else if (file.getName().endsWith(".java")) {
                    cuProcessor.processJavaFile(file);
                }
            }
        }
    }
}
