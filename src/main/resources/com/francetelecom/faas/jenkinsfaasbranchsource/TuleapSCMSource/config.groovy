package com.francetelecom.faas.jenkinsfaasbranchsource.TuleapSCMSource


def f = namespace(lib.FormTagLib)
def c = namespace(lib.CredentialsTagLib)
def scm = namespace(jenkins.scm.api.FormTagLib)

f.entry(title:_("Credentials"), field:"credentialsId", use:"required"){
    c.select(checkMethod:"POST")
}

f.entry(title:_("Project"), field:"projectId", use:"required"){
    f.select()
}

f.entry(title:_("Repo"), field:"repositoryPath"){
    f.select()
}

f.entry(title: _("Behaviors")){
    scm.traits(field:"traits")
}
