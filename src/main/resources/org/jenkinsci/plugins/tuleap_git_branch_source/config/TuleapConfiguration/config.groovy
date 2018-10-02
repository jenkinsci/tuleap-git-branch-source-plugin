package org.jenkinsci.plugins.tuleap_git_branch_source.config.TuleapConfiguration

def f = namespace(lib.FormTagLib);
def c = namespace(lib.CredentialsTagLib)


f.section(title: descriptor.displayName, help: descriptor.getHelpFile()) {

    f.entry(title: _("Domain_Url"), field: "domainUrl") {
        f.textbox(default: org.jenkinsci.plugins.tuleap_git_branch_source.client.TuleapClient.DEFAULT_TULEAP_DOMAIN_URL)

    }

    f.block() {
        f.validateButton(
                title: _("Test_connection"),
                progress: _("Testing"),
                method: "verifyUrls",
                with: "domainUrl"
        )
    }
}
