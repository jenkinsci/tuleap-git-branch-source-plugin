package org.jenkinsci.plugins.tuleap_git_branch_source;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.jvnet.hudson.test.JenkinsRule;

import jenkins.model.Jenkins;

public abstract class TuleapBranchSourceTest<T> {

    @ClassRule
    public static JenkinsRule r = new JenkinsRule();
    @Rule
    public TestName currentTestName = new TestName();

    protected T instance;

    public T load() {
        return load(currentTestName.getMethodName());
    }

    private T load(String dataSet) {
        return (T) Jenkins.XSTREAM2.fromXML(
            getClass().getResource(getClass().getSimpleName()+ "/" + dataSet + ".xml"));
    }

    @Before
    public void setup(){
        this.instance = load();
    }
}
