@TemplateRegistrations({
    @TemplateRegistration(
            folder = "IDE",
            //iconBase="org/myorg/additionalfiletemplates/Datasource.gif",
            displayName = "#HTMLtemplate_displayName",
            content = "HTML.html",
            description = "Description.html",
            scriptEngine = "freemarker"
    ),
    @TemplateRegistration(
            folder = "IDE",
            //iconBase="org/myorg/additionalfiletemplates/Datasource.gif",
            displayName = "#JavaTemplate_displayName",
            content = "Template.java.template",
            description = "Description.html",
            scriptEngine = "freemarker"
    ),
    @TemplateRegistration(
            folder = "IDE",
            //iconBase="org/myorg/additionalfiletemplates/Datasource.gif",
            displayName = "#InterfaceTemplate_displayName",
            content = "InterfaceTemplate.java.template",
            description = "Description.html",
            scriptEngine = "freemarker"
    ),
    @TemplateRegistration(
            folder = "IDE",
            //iconBase="org/myorg/additionalfiletemplates/Datasource.gif",
            displayName = "#TXTTemplate_displayName",
            content = "TXTTemplate.txt.template",
            description = "Description.html",
            scriptEngine = "freemarker"
    )

})
@Messages({"HTMLtemplate_displayName=Archivo HTML vacío",
    "JavaTemplate_displayName=Clase Java",
    "InterfaceTemplate_displayName=Interfaz Java",
    "TXTTemplate_displayName=Archivo TXT vacío"
})
package org.ide.additionalfiletemplates;

import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.api.templates.TemplateRegistrations;
import org.openide.util.NbBundle.Messages;
