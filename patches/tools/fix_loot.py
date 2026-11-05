#!/usr/bin/env python3
"""Rewrite the 102 Iron Shulker Boxes loot tables inside a jar to the 26.3 format.

26.2 used  "functions": [ { "function": "minecraft:copy_components", ... } ]
26.3 uses  "modifier":   { "type": "minecraft:copy_components", ... }

The old key is silently ignored by the 26.3 codec, so the dropped box has no
minecraft:container component and the contents are lost.

Usage: python fix_loot.py path/to/ironshulkerbox.jar
"""
import json
import sys
import zipfile
import shutil
from pathlib import Path

INCLUDE = [
    "minecraft:custom_name",
    "minecraft:container",
    "minecraft:lock",
    "minecraft:container_loot",
]
PREFIX = "data/ironshulkerbox/loot_table/blocks/"


def rewrite(table):
    entry = table["pools"][0]["entries"][0]
    name = entry["name"]
    namespace, _, path = name.partition(":")
    sequence = table.get("random_sequence") or namespace + ":blocks/" + path
    return {
        "type": "minecraft:block",
        "pools": [
            {
                "rolls": 1,
                "entries": [
                    {
                        "type": "minecraft:item",
                        "modifier": {
                            "type": "minecraft:copy_components",
                            "include": list(INCLUDE),
                            "source": "block_entity",
                        },
                        "name": name,
                    }
                ],
            }
        ],
        "random_sequence": sequence,
    }


def main(jar_path):
    jar = Path(jar_path)
    temp = jar.with_suffix(".tmp")
    changed = 0
    with zipfile.ZipFile(jar, "r") as src, zipfile.ZipFile(temp, "w", zipfile.ZIP_DEFLATED) as dst:
        for info in src.infolist():
            data = src.read(info.filename)
            if info.filename.startswith(PREFIX) and info.filename.endswith(".json"):
                table = rewrite(json.loads(data.decode("utf-8")))
                data = json.dumps(table, indent=2).encode("utf-8")
                changed += 1
            dst.writestr(info, data)
    shutil.move(str(temp), str(jar))
    print("rewrote", changed, "loot tables")


if __name__ == "__main__":
    main(sys.argv[1])
