@TemplateRegistrations({
    @TemplateRegistration(
        folder = "Custom",
        //iconBase="org/myorg/additionalfiletemplates/Datasource.gif",
        displayName = "#HTMLtemplate_displayName",
        content = "HTML.html",
        description = "Description.html",
        scriptEngine="freemarker"
    ),
    @TemplateRegistration(
        folder = "Custom",
        //iconBase="org/myorg/additionalfiletemplates/Datasource.gif",
        displayName = "#JavaTemplate_displayName",
        content = "Template.java.template",
        description = "Description.html",
        scriptEngine="freemarker"
    ),
})
@Messages({"HTMLtemplate_displayName=Empty HTML file",
        "JavaTemplate_displayName=Archivo Java vacio"
        })
package org.ide.additionalfiletemplates;

import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.api.templates.TemplateRegistrations;
import org.openide.util.NbBundle.Messages;