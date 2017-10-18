package com.francetelecom.faas.jenkinsfaasbranchsource.config.OFConfiguration

def f = namespace(lib.FormTagLib);
def c = namespace(lib.CredentialsTagLib)


f.section(title: descriptor.displayName) {
    f.entry(title: _("OrangeForge Servers"),
            help: descriptor.getHelpFile()) {


        f.entry(title: _("Name"), field: "name") {
            f.textbox()
        }

        f.entry(title: _("API URL"), field: "apiBaseUrl") {
            f.textbox(default: com.francetelecom.faas.jenkinsfaasbranchsource.config.OFConfiguration.ORANGEFORGE_API_URL)

        }

        f.entry(title: _("GIT HTTPS URL"), field: "gitBaseUrl") {
            f.textbox(default: com.francetelecom.faas.jenkinsfaasbranchsource.config.OFConfiguration.ORANGEFORGE_GIT_HTTPS_URL)
        }

        f.entry(title: _("Credentials"), field: "credentialsId") {
            c.select(context:app, includeUser:false, expressionAllowed:false)
        }

        f.block() {
            f.validateButton(
                    title: _("Test connection"),
                    progress: _("Testing..."),
                    method: "verifyCredentials",
                    with: "apiBaseUrl,gitBaseUrl,credentialsId"
            )
        }
    }
}
