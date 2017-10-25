package com.francetelecom.faas.jenkinsfaasbranchsource.config.OFConfiguration

def f = namespace(lib.FormTagLib);
def c = namespace(lib.CredentialsTagLib)


f.section(title: descriptor.displayName, help: descriptor.getHelpFile()) {

    f.entry(title: _("Name"), field: "name") {
        f.textbox()
    }

    f.entry(title: _("Api_url"), field: "apiBaseUrl") {
        f.textbox(default: com.francetelecom.faas.jenkinsfaasbranchsource.config.OFConfiguration.ORANGEFORGE_API_URL)

    }

    f.entry(title: _("Git_https_url"), field: "gitBaseUrl") {
        f.textbox(default: com.francetelecom.faas.jenkinsfaasbranchsource.config.OFConfiguration.ORANGEFORGE_GIT_HTTPS_URL)
    }

    f.entry(title: _("Credentials"), field: "credentialsId") {
        c.select(context:app, includeUser:false, expressionAllowed:false)
    }

    f.block() {
        f.validateButton(
                title: _("Test_connection"),
                progress: _("Testing"),
                method: "verifyCredentials",
                with: "apiBaseUrl,gitBaseUrl,credentialsId"
        )
    }
}
