#!/usr/bin/env python3
"""Validate English/Simplified Chinese resources using only Python's standard library."""
from collections import Counter
from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ET

ROOT = next(parent for parent in Path(__file__).resolve().parents if (parent / "settings.gradle").exists())
KINDS = ("string", "string-array", "plurals")
FORMAT = re.compile(r"%(?:(\d+)\$)?[-#+ 0,(]*\d*(?:\.\d+)?([a-zA-Z%])")
ANDROID = "{http://schemas.android.com/apk/res/android}"
errors = []


def read_resources(path):
    entries = {}
    for file in path.glob("*.xml"):
        for element in ET.parse(file).getroot():
            if element.tag not in KINDS:
                continue
            name = element.attrib["name"]
            if name in entries:
                errors.append(f"Duplicate resource: {file}:{name}")
            entries[name] = element
    return entries


def placeholders(text):
    result = []
    implicit = 0
    for index, kind in FORMAT.findall(text):
        if kind == "%":
            continue
        implicit += 1
        result.append((int(index) if index else implicit, kind))
    return Counter(result), Counter(re.findall(r"(?<!\^)\^[1-9]", text))


def compare(module, key, english, chinese):
    if english.attrib.get("formatted") == "false":
        return
    if placeholders("".join(english.itertext())) != placeholders("".join(chinese.itertext())):
        errors.append(f"Format arguments differ: {module}:{key}")


total = 0
for module in ("manager", "core/ui", "database", "compat"):
    res = ROOT / module / "src/main/res"
    english = read_resources(res / "values")
    chinese = read_resources(res / "values-zh-rCN")
    required = {key: value for key, value in english.items() if value.attrib.get("translatable") != "false"}
    for key in required.keys() - chinese.keys():
        errors.append(f"Missing Chinese: {module}:{key}")
    for key in required.keys() & chinese.keys():
        a, b = required[key], chinese[key]
        if a.tag != b.tag:
            errors.append(f"Resource types differ: {module}:{key}")
        elif a.tag == "string":
            compare(module, key, a, b)
        elif a.tag == "string-array":
            if len(a) != len(b):
                errors.append(f"Array length differs: {module}:{key}")
            for index, (first, second) in enumerate(zip(a, b)):
                compare(module, f"{key}[{index}]", first, second)
                if key.endswith(("_value", "_values")) and first.text != second.text:
                    errors.append(f"Stored option value changed: {module}:{key}[{index}]")
        else:
            quantities = {item.attrib["quantity"]: item for item in a}
            for item in b:
                quantity = item.attrib["quantity"]
                compare(module, f"{key}:{quantity}", quantities.get(quantity, quantities["other"]), item)
    for path in res.rglob("*.xml"):
        if path.parent.name.startswith("values"):
            continue
        for node in ET.parse(path).iter():
            for attribute in ("title", "summary", "summaryOn", "summaryOff", "text", "hint", "contentDescription", "label"):
                value = node.attrib.get(ANDROID + attribute, "")
                if (re.search("[A-Za-z]", value) and not value.startswith(("@", "?", "/", "http")) and value != "%s"):
                    errors.append(f"Hardcoded UI text: {path.relative_to(ROOT)}:{attribute}={value}")
    total += len(required)
    print(f"{module}: {len(required)} translatable resources, Chinese complete")

if errors:
    print("\n".join(errors), file=sys.stderr)
    sys.exit(1)
print(f"PASS: {total} resources; no missing Chinese, duplicate names, placeholder mismatches, option-value changes, or hardcoded XML UI labels.")
