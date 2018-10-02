package org.jenkinsci.plugins.tuleap_git_branch_source.trait.UserForkRepositoryTrait

def f = namespace(lib.FormTagLib)

f.entry(title: _("Strategy"), field: "strategy") {
    f.select(default: "1")
}
