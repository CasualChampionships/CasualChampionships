package net.casualuhc.uhc.util

import net.casualuhc.arcade.Arcade
import net.minecraft.nbt.NbtIo
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate
import java.nio.file.Files
import java.nio.file.Path

object StructureUtils {
    @Deprecated("")
    fun read(path: Path): StructureTemplate {
        val inputStream = Files.newInputStream(path)
        val structureNBT = NbtIo.readCompressed(inputStream)
        return Arcade.server.structureManager.readStructure(structureNBT)
    }
}