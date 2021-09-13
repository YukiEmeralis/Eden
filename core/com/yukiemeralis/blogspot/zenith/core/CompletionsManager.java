package com.yukiemeralis.blogspot.zenith.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yukiemeralis.blogspot.zenith.Zenith;
import com.yukiemeralis.blogspot.zenith.module.ZenithModule;
import com.yukiemeralis.blogspot.zenith.utils.Option;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils;
import com.yukiemeralis.blogspot.zenith.utils.PrintUtils.InfoType;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@SuppressWarnings("unused") // "Unused" methods are instead used reflectively
public class CompletionsManager 
{
    private static CompletionsManager instance = new CompletionsManager(); 

    private static Map<String, ObjectMethodPair> completionLists = new HashMap<>() 
    {{
        try {
            put("BOOLEAN", new ObjectMethodPair(instance, instance.getClass().getDeclaredMethod("booleans")));
            put("ONLINE_PLAYERS", new ObjectMethodPair(instance, instance.getClass().getDeclaredMethod("onlinePlayers")));
            put("ALL_PLAYERS", new ObjectMethodPair(instance, instance.getClass().getDeclaredMethod("allPlayers")));
            put("ALL_MODULES", new ObjectMethodPair(instance, instance.getClass().getDeclaredMethod("allModules")));
            put("ENABLED_MODULES", new ObjectMethodPair(instance, instance.getClass().getDeclaredMethod("enabledModules")));
            put("DISABLED_MODULES", new ObjectMethodPair(instance, instance.getClass().getDeclaredMethod("disabledModules")));
        } catch (NoSuchMethodException e) {
            PrintUtils.log("(Failed to generate completions.)", InfoType.ERROR);
            PrintUtils.printPrettyStacktrace(e);
        }
    }};

    public static boolean hasCompletion(String key)
    {
        return completionLists.containsKey(key);
    }

    public static Option<String> registerCompletion(String label, ObjectMethodPair data, boolean overwrite)
    {
        Option<String> option = new Option<>(String.class);

        if (completionLists.containsKey(label) && !overwrite)
        {
            option.some("Completion with this name already exists.");
            return option;
        }

        completionLists.put(label, data);
        return option;
    }

    @SuppressWarnings("unchecked")
    public static List<String> getCompletions(String key)
    {
        if (!completionLists.containsKey(key))
            return new ArrayList<>();

        try {
            return (List<String>) completionLists.get(key).invoke();
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | ClassCastException e) {
            PrintUtils.printPrettyStacktrace(e);
            return new ArrayList<>() {{ add("An error has occurred."); }};
        }
    }

    // Base completions

    private static final List<String> booleans = new ArrayList<>(), onlinePlayers = new ArrayList<>();

    static {
        booleans.add("true");
        booleans.add("false");
    }

    private List<String> booleans()
    {
        return booleans;
    }

    private List<String> onlinePlayers()
    {
        List<String> buffer = new ArrayList<>();

        for (Player p : Bukkit.getOnlinePlayers()) 
            buffer.add(p.getName());

        return buffer;
    }

    private List<String> allPlayers()
    {
        List<String> buffer = new ArrayList<>();

        buffer.addAll(Zenith.getUuidCache().keySet());
        for (Player p : Bukkit.getOnlinePlayers())
            if (!buffer.contains(p.getName()))
                buffer.add(p.getName());

        return buffer;
    }

    private List<String> allModules()
    {
        List<String> buffer = new ArrayList<>();

        for (ZenithModule mod : Zenith.getModuleManager().getAllModules())
            buffer.add(mod.getName());

        return buffer;
    }

    private List<String> enabledModules()
    {
        List<String> buffer = new ArrayList<>();

        for (ZenithModule mod : Zenith.getModuleManager().getEnabledModules())
            buffer.add(mod.getName());

        return buffer;
    }

    private List<String> disabledModules()
    {
        List<String> buffer = new ArrayList<>();

        for (ZenithModule mod : Zenith.getModuleManager().getDisabledModules())
            buffer.add(mod.getName());

        return buffer;
    }

    /**
     * A data object containing an object and a method belonging to that object.
     */
    public static class ObjectMethodPair
    {
        private final Object obj;
        private final Method method;

        public ObjectMethodPair(Object obj, Method method)
        {
            this.method = method;
            this.obj = obj;
        }

        /**
         * Convenience constructor that obtains a class method by name.
         * @param obj
         * @param methodName
         * @throws NoSuchMethodException
         */
        public ObjectMethodPair(Object obj, String methodName) throws NoSuchMethodException
        {
            this(obj, obj.getClass().getDeclaredMethod(methodName));
        }

        public Object invoke(Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
        {
            method.setAccessible(true);
            return method.invoke(obj, args);
        }
    }
}
