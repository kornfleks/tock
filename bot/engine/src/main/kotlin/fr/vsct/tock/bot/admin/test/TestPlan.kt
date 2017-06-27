/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.bot.admin.test

import fr.vsct.tock.bot.admin.dialog.DialogReport
import fr.vsct.tock.bot.engine.message.Message

/**
 *
 */
data class TestPlan(
        val dialogs: List<DialogReport>,
        val name: String,
        val applicationId: String,
        val namespace: String,
        val nlpModel: String,
        val botApplicationConfigurationId: String,
        val startAction: Message? = null,
        val _id: String? = null
) {
}