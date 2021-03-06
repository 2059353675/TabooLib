package me.skymc.taboolib.common.inject;

import com.google.common.collect.Maps;
import com.ilummc.tlib.logger.TLogger;
import me.skymc.taboolib.TabooLibLoader;
import me.skymc.taboolib.commands.builder.SimpleCommandBuilder;
import me.skymc.taboolib.common.configuration.TConfiguration;
import me.skymc.taboolib.common.packet.TPacketHandler;
import me.skymc.taboolib.common.packet.TPacketListener;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * @Author sky
 * @Since 2018-10-05 13:40
 */
public class TInjectLoader implements TabooLibLoader.Loader {

    private static Map<Class<?>, TInjectTask> injectTypes = Maps.newHashMap();

    static {
        // TLogger Inject
        injectTypes.put(TLogger.class, (plugin, field, args) -> {
            try {
                field.set(null, TLogger.getUnformatted(plugin));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        // TPacketListener Inject
        injectTypes.put(TPacketListener.class, (plugin, field, args) -> {
            try {
                TPacketHandler.addListener(plugin, ((TPacketListener) field.get(null)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        // TConfiguration Inject
        injectTypes.put(TConfiguration.class, (plugin, field, args) -> {
            if (args.length == 0) {
                TLogger.getGlobalLogger().error("Invalid inject arguments: " + field.getName());
                return;
            }
            try {
                field.set(null, TConfiguration.createInResource(plugin, args[0]));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        // SimpleCommandBuilder Inject
        injectTypes.put(SimpleCommandBuilder.class, (plugin, field, args) -> {
            try {
                ((SimpleCommandBuilder) field.get(null)).build();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void load(Plugin plugin, Class<?> pluginClass) {
        for (Field declaredField : pluginClass.getDeclaredFields()) {
            TInject annotation = declaredField.getAnnotation(TInject.class);
            if (annotation == null) {
                continue;
            }
            if (!Modifier.isStatic(declaredField.getModifiers())) {
                TLogger.getGlobalLogger().error(declaredField.getName() + " is not a static field.");
                continue;
            }
            TInjectTask tInjectTask = injectTypes.get(declaredField.getType());
            if (tInjectTask == null) {
                TLogger.getGlobalLogger().error(declaredField.getName() + " cannot inject.");
                continue;
            }
            try {
                declaredField.setAccessible(true);
                tInjectTask.run(plugin, declaredField, annotation.value());
            } catch (Exception e) {
                TLogger.getGlobalLogger().error(declaredField.getName() + " inject failed: " + e.getMessage());
            }
        }
    }
}
