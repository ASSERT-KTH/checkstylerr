/*
 * Copyright (c) 2018 Daniel Ennis (Aikar) MIT License
 *
 *  Permission is hereby granted, free of charge, to any person obtaining
 *  a copy of this software and associated documentation files (the
 *  "Software"), to deal in the Software without restriction, including
 *  without limitation the rights to use, copy, modify, merge, publish,
 *  distribute, sublicense, and/or sell copies of the Software, and to
 *  permit persons to whom the Software is furnished to do so, subject to
 *  the following conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 *  LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 *  OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 *  WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.destroystokyo.paper.event.profile;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Set;
import org.jetbrains.annotations.NotNull;

/**
 * Fired once a profiles additional properties (such as textures) has been filled
 */
public class FillProfileEvent extends Event {
    @NotNull private final PlayerProfile profile;

    public FillProfileEvent(@NotNull PlayerProfile profile) {
        super(!org.bukkit.Bukkit.isPrimaryThread());
        this.profile = profile;
    }

    /**
     * @return The Profile that had properties filled
     */
    @NotNull
    public PlayerProfile getPlayerProfile() {
        return profile;
    }

    /**
     * Same as .getPlayerProfile().getProperties()
     *
     * @see PlayerProfile#getProperties()
     * @return The new properties on the profile.
     */
    @NotNull
    public Set<ProfileProperty> getProperties() {
        return profile.getProperties();
    }

    private static final HandlerList handlers = new HandlerList();

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
