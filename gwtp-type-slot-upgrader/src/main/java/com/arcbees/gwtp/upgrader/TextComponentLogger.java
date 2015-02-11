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

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import javax.swing.JTextArea;

public class TextComponentLogger extends Handler {
    private final JTextArea text;

    TextComponentLogger(JTextArea text) {
        this.setFormatter(new SimpleFormatter());
        this.text = text;
    }

    @Override
    public void publish(LogRecord record) {
        if (isLoggable(record)) {
            String message = getFormatter().format(record); 
            text.append(message);
        }
    }

    @Override
    public void flush() {/**/
    }

    @Override
    public void close() throws SecurityException {/**/
    }
}
