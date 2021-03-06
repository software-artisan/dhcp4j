/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.v6.options;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * See http://www.iana.org/assignments/bootp-dhcp-parameters/bootp-dhcp-parameters.xhtml#options
 *
 * @author shevek
 * @author marosmars
 * @author marekgr
 */
public class Dhcp6OptionsRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(Dhcp6OptionsRegistry.class);

    @SuppressWarnings("unchecked")
    private static class Inner {

        private static final Dhcp6OptionsRegistry INSTANCE = new Dhcp6OptionsRegistry();

        @SuppressWarnings("rawtypes")
        private static final Class OPTION_CLASSES[] = { IaNaOption.class, ClientIdOption.class, ServerIdOption.class,
                        ElapsedTimeOption.class, IaTaOption.class, IaAddressOption.class, NewPOSIXTimezone.class,
                        NewTZDBTimezone.class, OptionRequestOption.class, StatusCodeOption.class,
                        RelayMessageOption.class, PreferenceOption.class, InterfaceIdOption.class,
                        ServerUnicastOption.class, UserClassOption.class, VendorClassOption.class,
                        VendorSpecificInformationOption.class };

        static {
            for (Class<? extends Dhcp6Option> optionType : OPTION_CLASSES) {
                INSTANCE.addOptionType(optionType);
            }
        }

    }

    @Nonnull
    public static Dhcp6OptionsRegistry getInstance() {
        return Inner.INSTANCE;
    }

    private final BiMap<Short, Class<? extends Dhcp6Option>> optionTypes = HashBiMap.create();

    @Nonnull
    public static <T extends Dhcp6Option> T newInstance(@Nonnull Class<T> type) {
        try {
            return type.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException("Cannot instantiate " + type, e);
        }
    }

    @Nonnull
    public static <T extends Dhcp6Option> T copy(@Nonnull Class<T> type, @Nonnull T option) {
        final T t = newInstance(type);
        t.setData(option.getData());
        return t;
    }

    private short getTagFrom(@Nonnull Class<? extends Dhcp6Option> type) {
        Dhcp6Option o = newInstance(type);
        return o.getTag();
    }

    public void addOptionType(@Nonnull Class<? extends Dhcp6Option> type) {
        short tag = getTagFrom(type);
        if (optionTypes.put(tag, type) != null)
            throw new IllegalArgumentException("Duplicate tag: " + type);
    }

    @CheckForNull
    public Class<? extends Dhcp6Option> getOptionType(short tag) {
        return optionTypes.get(tag);
    }

    @Nonnull
    public short getOptionTag(@Nonnull Class<? extends Dhcp6Option> type) {
        Short tag = optionTypes.inverse().get(type);
        if (tag != null)
            return tag;
        LOG.warn("Unregistered option type: {}", type);
        return getTagFrom(type);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + optionTypes + ")";
    }

}
