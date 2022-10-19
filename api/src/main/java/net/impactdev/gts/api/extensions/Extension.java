/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package net.impactdev.gts.api.extensions;

/**
 * Represents an extension that is meant to hook into GTS.
 *
 * An extension is not a plugin, but rather a component capable of providing extra functionality for GTS.
 *
 * An extension must contain a resource titled "extension.json",
 */
public interface Extension {

    /**
     * Responsible for constructing the extension.
     */
    void construct();

    /**
     * Enables the extension, and is fired after load.
     */
    void enable();

    /**
     * Unloads the extension. Fired during server shutdown
     */
    void shutdown();

//    /**
//     * Specifies an additional list of dependencies that this extension will require to function
//     *
//     * @return The list of dependencies required for normal operations of the extension
//     */
//    List<Dependency> getRequiredDependencies();

//    /**
//     * Returns a set of command executors that this extension would like to supply
//     *
//     * @return A set of executors for the extension, if any
//     */
//    Set<GTSCommandExecutor<?, ?, ?>> getExecutors();
//
//    /**
//     * Supplies a set of extended information regarding the environment that this extension might make use of
//     */
//    void getExtendedEnvironmentInformation(Environment environment);

}
