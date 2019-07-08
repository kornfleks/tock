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

package fr.vsct.tock.bot.connector.rest

import fr.vsct.tock.bot.connector.Connector
import fr.vsct.tock.bot.connector.ConnectorConfiguration
import fr.vsct.tock.bot.connector.ConnectorProvider
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.connector.ConnectorTypeConfiguration
import fr.vsct.tock.shared.resourceAsString


/**
 * The [RestConnector] provider.
 */
internal object RestConnectorProvider : ConnectorProvider {

    override val connectorType: ConnectorType get() = ConnectorType.rest

    override fun connector(connectorConfiguration: ConnectorConfiguration): Connector {
        return RestConnector(
            connectorConfiguration.connectorId,
            connectorConfiguration.path,
            createRequestFilter(connectorConfiguration)
        )
    }

    override fun check(connectorConfiguration: ConnectorConfiguration): List<String> =
        super.check(connectorConfiguration) +
            listOfNotNull(
                if (connectorConfiguration.ownerConnectorType == null)
                    "rest connector must have an owner connector type"
                else null
            )

    override fun configuration(): ConnectorTypeConfiguration =
        ConnectorTypeConfiguration(
            ConnectorType.rest,
            ConnectorTypeConfiguration.commonSecurityFields(),
            resourceAsString("/test.svg")
        )

}

internal class RestConnectorProviderService : ConnectorProvider by RestConnectorProvider