/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.datacleaner.monitor.server.wizard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.monitor.shared.model.DCUserInputException;
import org.eobjects.datacleaner.monitor.wizard.WizardPageController;
import org.eobjects.datacleaner.monitor.wizard.common.AbstractFreemarkerWizardPage;

/**
 * Page for entering SugarCRM credentials
 */
final class SugarCrmDatastoreCredentialsPage extends AbstractFreemarkerWizardPage {

    private final SugarCrmDatastoreWizardSession _session;

    public SugarCrmDatastoreCredentialsPage(SugarCrmDatastoreWizardSession session) {
        _session = session;
    }

    @Override
    public Integer getPageIndex() {
        return 1;
    }

    @Override
    public WizardPageController nextPageController(Map<String, List<String>> formParameters)
            throws DCUserInputException {
        String username = formParameters.get("sugarcrm_username").get(0);
        String password = formParameters.get("sugarcrm_password").get(0);
        
        if (StringUtils.isNullOrEmpty(username)) {
            throw new DCUserInputException("Please provide a valid username.");
        }
        if (StringUtils.isNullOrEmpty(password)) {
            throw new DCUserInputException("Please provide a valid password.");
        }

        _session.setCredentials(username, password);

        return new DatastoreNameAndDescriptionWizardPage(_session.getWizardContext(), getPageIndex() + 1, "SugarCRM",
                "Connects to the web services of SugarCRM") {
            @Override
            protected WizardPageController nextPageController(String name, String description) {
                _session.setName(name);
                _session.setDescription(description);
                return null;
            }
        };
    }

    @Override
    protected String getTemplateFilename() {
        return "SugarCrmDatastoreCredentialsPage.html";
    }

    @Override
    protected Map<String, Object> getFormModel() {
        return new HashMap<String, Object>();
    }

}
