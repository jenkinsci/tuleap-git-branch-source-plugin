package com.francetelecom.faas.jenkinsfaasbranchsource.trait.OFUserForkRepositoryTrait

def f = namespace(lib.FormTagLib)

f.entry(title: _("Strategy"), field: "strategy") {
    f.select(default: "1")
}
