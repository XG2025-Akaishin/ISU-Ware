package me.aidan.sydney.commands.impl;

import me.aidan.sydney.Sydney;
import me.aidan.sydney.commands.Command;
import me.aidan.sydney.commands.RegisterCommand;
import me.aidan.sydney.modules.Module;
import me.aidan.sydney.utils.chat.ChatUtils;

import java.util.ArrayList;

@RegisterCommand(name = "drawn", tag = "Drawn", description = "Manages the modules that will be shown on the HUD's module list.", syntax = "<true|false|list|reset> | <[module]> <true|false|reset>")
public class DrawnCommand extends Command {
    @Override
    public void execute(String[] args) {
        if (args.length == 2) {
            Module module = Sydney.MODULE_MANAGER.getModule(args[0]);
            if (module == null) {
                Sydney.CHAT_MANAGER.tagged("Could not find the module specified.", getTag(), getName());
                return;
            }

            switch (args[1]) {
                case "true" -> {
                    module.drawn.setValue(true);
                    Sydney.CHAT_MANAGER.tagged("Successfully set the module's drawn status to " + ChatUtils.getPrimary() + "true" + ChatUtils.getSecondary() + ".", getTag(), getName());
                }
                case "false" -> {
                    module.drawn.setValue(false);
                    Sydney.CHAT_MANAGER.tagged("Successfully set the module's drawn status to " + ChatUtils.getPrimary() + "false" + ChatUtils.getSecondary() + ".", getTag(), getName());
                }
                case "reset" -> {
                    module.drawn.setValue(module.drawn.getDefaultValue());
                    Sydney.CHAT_MANAGER.tagged("Successfully set the module's drawn status to it's default value.", getTag(), getName());
                }
                default -> messageSyntax();
            }
        } else if (args.length == 1) {
            switch (args[0]) {
                case "true" -> {
                    for (Module module : Sydney.MODULE_MANAGER.getModules()) module.drawn.setValue(true);
                    Sydney.CHAT_MANAGER.tagged("Successfully set every module's drawn status to " + ChatUtils.getPrimary() + "true" + ChatUtils.getSecondary() + ".", getTag(), getName());
                }
                case "false" -> {
                    for (Module module : Sydney.MODULE_MANAGER.getModules()) module.drawn.setValue(false);
                    Sydney.CHAT_MANAGER.tagged("Successfully set every module's drawn status to " + ChatUtils.getPrimary() + "false" + ChatUtils.getSecondary() + ".", getTag(), getName());
                }
                case "list" -> {
                    ArrayList<Module> drawnModules = new ArrayList<>(Sydney.MODULE_MANAGER.getModules().stream().filter(m -> m.drawn.getValue()).toList());

                    if (drawnModules.isEmpty()) {
                        Sydney.CHAT_MANAGER.tagged("There are currently no modules with their drawn status set to " + ChatUtils.getPrimary() + "true" + ChatUtils.getSecondary() + ".", getTag(), getName() + "-list");
                    } else {
                        StringBuilder modulesString = new StringBuilder();
                        int index = 0;

                        for (Module module : drawnModules) {
                            modulesString.append(ChatUtils.getSecondary()).append(module.getName()).append(index + 1 == drawnModules.size() ? "" : ", ");
                            index++;
                        }

                        Sydney.CHAT_MANAGER.message(ChatUtils.getSecondary() + "Drawn Modules " + ChatUtils.getPrimary() + "[" + ChatUtils.getSecondary() + drawnModules.size() + ChatUtils.getPrimary() + "]: " + ChatUtils.getSecondary() + modulesString, getName() + "-list");
                    }
                }
                case "reset" -> {
                    for (Module module : Sydney.MODULE_MANAGER.getModules()) module.drawn.setValue(module.drawn.getDefaultValue());
                    Sydney.CHAT_MANAGER.tagged("Successfully set every module's drawn status to it's default value.", getTag(), getName());
                }
                default -> messageSyntax();
            }
        } else {
            messageSyntax();
        }
    }
}
