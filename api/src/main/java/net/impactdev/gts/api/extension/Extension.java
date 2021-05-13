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

package net.impactdev.gts.api.extension;

import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.commands.GTSCommandExecutor;
import net.impactdev.gts.api.environment.Environment;
import net.impactdev.impactor.api.dependencies.Dependency;
import net.impactdev.impactor.api.plugin.ImpactorPlugin;
import net.impactdev.impactor.api.plugin.components.Configurable;
import net.impactdev.impactor.api.plugin.components.Translatable;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * Represents an extension that is meant to hook into GTS
 *
 * An extension is not a plugin, but rather a component capable of providing extra functionality for GTS.
 *
 * An extension must contain a resource titled "extension.json",
 */
public interface Extension extends ImpactorPlugin, Configurable, Translatable {

    /**
     * Loads the extension. Fired during server startup
     *
     * @param service A direct reference to the API Service provided to remove one additional call for
     *                those lazy people
     */
    void load(GTSService service, Path dataDir);

    /**
     * Enables the extension, and is fired after load.
     *
     * @param service A direct reference to the API Service provided to remove one additional call for
     *                those lazy people
     */
    void enable(GTSService service);

    /**
     * Unloads the extension. Fired during server shutdown
     */
    void unload();

    /**
     * Specifies an additional list of dependencies that this extension will require to function
     *
     * @return The list of dependencies required for normal operations of the extension
     */
    List<Dependency> getRequiredDependencies();

    /**
     * Returns a set of command executors that this extension would like to supply
     *
     * @return A set of executors for the extension, if any
     */
    Set<GTSCommandExecutor<?, ?>> getExecutors();

    /**
     * Supplies a set of extended information regarding the environment that this extension might make use of
     */
    void getExtendedEnvironmentInformation(Environment environment);

}
