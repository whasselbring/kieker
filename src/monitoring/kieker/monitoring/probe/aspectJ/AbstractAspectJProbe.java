package kieker.monitoring.probe.aspectJ;

import kieker.monitoring.probe.IMonitoringProbe;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/*
 *
 * ==================LICENCE=========================
 * Copyright 2006-2009 Kieker Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ==================================================
 */

/**
 * @author Jan Waller
 */
@Aspect
public abstract class AbstractAspectJProbe implements IMonitoringProbe {

    @Pointcut("!within(kieker.common..*)"
        + " && !within(kieker.monitoring..*)"
        + " && !within(kieker.analysis..*)"
        + " && !within(kieker.tools..*)")
    public void notWithinKieker() {
    }
}
