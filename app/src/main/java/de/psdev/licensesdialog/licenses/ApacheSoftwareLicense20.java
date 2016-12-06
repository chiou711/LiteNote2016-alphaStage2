/*
 * Copyright 2013 Philip Schiffer
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package de.psdev.licensesdialog.licenses;

import com.cwc.litenote.R;

import android.content.Context;

public class ApacheSoftwareLicense20 extends License {

    private static final long serialVersionUID = 4854000061990891449L;

    @Override
    public String getName() {
        return "Apache Software License 2.0";
    }

    @Override
    public String getSummaryText(final Context context) {
        return getContent(context, R.raw.asl_20_summary);
    }

    @Override
    public String getFullText(final Context context) {
        return getContent(context, R.raw.asl_20_full);
    }

    @Override
    public String getVersion() {
        return "2.0";
    }

    @Override
    public String getUrl() {
        return "http://www.apache.org/licenses/LICENSE-2.0.txt";
    }


}
