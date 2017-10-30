package com.francetelecom.faas.jenkinsfaasbranchsource.trait.UserForkRepositoryTrait

def f = namespace(lib.FormTagLib)

f.entry(title: _("Strategy"), field: "strategy") {
    f.select(default: "1")
}
