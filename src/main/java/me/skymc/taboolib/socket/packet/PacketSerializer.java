package me.skymc.taboolib.socket.packet;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.TabooLibLoader;
import me.skymc.taboolib.listener.TListener;
import me.skymc.taboolib.socket.packet.impl.PacketEmpty;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * @Author sky
 * @Since 2018-08-22 23:32
 */
@TListener
public class PacketSerializer implements Listener {

    private static PacketParser parser = new PacketParser();

    public PacketSerializer() {
        loadPacket();
    }

    @EventHandler
    public void onEnable(PluginEnableEvent e) {
        loadPacket(e.getPlugin());
    }

    @EventHandler
    public void onDisable(PluginDisableEvent e) {
        unloadPacket(e.getPlugin());
    }

    public static void loadPacket() {
        Arrays.stream(Bukkit.getPluginManager().getPlugins()).forEach(PacketSerializer::loadPacket);
    }

    public static void loadPacket(Plugin plugin) {
        if (TabooLib.isTabooLib(plugin) || TabooLib.isDependTabooLib(plugin)) {
            TabooLibLoader.getPluginClasses(plugin).ifPresent(classes -> classes.stream().filter(pluginClass -> pluginClass.isAnnotationPresent(PacketType.class)).forEach(pluginClass -> parser.getPackets().add(pluginClass)));
        }
    }

    public static void unloadPacket() {
        Arrays.stream(Bukkit.getPluginManager().getPlugins()).forEach(PacketSerializer::unloadPacket);
    }

    public static void unloadPacket(Plugin plugin) {
        if (TabooLib.isTabooLib(plugin) || TabooLib.isDependTabooLib(plugin)) {
            TabooLibLoader.getPluginClasses(plugin).ifPresent(classes -> classes.stream().filter(pluginClass -> pluginClass.isAnnotationPresent(PacketType.class)).forEach(pluginClass -> parser.getPackets().remove(pluginClass)));
        }
    }

    public static String serialize(Packet packet) {
        JsonObject json = new JsonObject();
        packet.serialize(json);
        json.addProperty("uid", packet.getUid());
        json.addProperty("port", packet.getPort());
        json.addProperty("packet", packet.getClass().getAnnotation(PacketType.class).name());
        Arrays.stream(packet.getClass().getDeclaredFields()).filter(field -> field.isAnnotationPresent(PacketValue.class)).forEach(field -> {
            field.setAccessible(true);
            try {
                Object obj = field.get(packet);
                if (obj instanceof Number) {
                    json.addProperty(field.getName(), (Number) obj);
                } else if (obj instanceof Boolean) {
                    json.addProperty(field.getName(), (Boolean) obj);
                } else if (obj instanceof String) {
                    json.addProperty(field.getName(), (String) obj);
                } else if (obj instanceof Character) {
                    json.addProperty(field.getName(), (Character) obj);
                } else {
                    System.out.println("Serialize: Invalid packet value: " + field.getName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return json.toString();
    }

    public static Packet unSerialize(String origin) {
        Packet packet = null;
        try {
            packet = parser.parser((JsonObject) new JsonParser().parse(origin));
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return packet == null ? new PacketEmpty(0) : packet;
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public static PacketParser getParser() {
        return parser;
    }
}
