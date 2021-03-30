/*
 * The MIT License
 *
 * Copyright 2016 CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jenkinsci.plugins.tuleap_git_branch_source;

import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.mixin.ChangeRequestSCMRevision;

/**
 * Revision of a pull request.
 *
 * @since 2.2.0
 */
public class TuleapPullRequestSCMRevision extends ChangeRequestSCMRevision<TuleapPullRequestSCMHead> {

    /**
     * Standardize serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param head   the head.
     * @param target the target revision.
     */
    public TuleapPullRequestSCMRevision(@NonNull TuleapPullRequestSCMHead head, @NonNull SCMRevision target) {
        super(head, target);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equivalent(ChangeRequestSCMRevision<?> o) {
        if (!(o instanceof TuleapPullRequestSCMRevision)) {
            return false;
        }
        TuleapPullRequestSCMRevision other = (TuleapPullRequestSCMRevision) o;
        return getHead().equals(other.getHead());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int _hashCode() {
        return getHead().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getHead().toString();
    }

}
