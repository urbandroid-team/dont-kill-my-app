---
layout: page
title: The core of the problem
permalink: /problem/
---

Smartphones are getting more and more powerful, but the battery capacity is lagging behind. Vendors are always trying to squeeze some battery saving features into the firmware with each new Android release. With Android 6 (Marshmallow), Google has introduced Doze mode to the base Android, in an attempt to unify battery saving across the various Android phones.

Unfortunately, vendors such as Samsung, Huawei, OnePlus (and more) did not seem to catch that ball and they all have their own battery savers, usually very poorly written, saving battery only superficially.

These battery saving features have lots of side effects. They usually kill long running processes – but don’t care whether the user wants the process to run or not (think sleep tracking, fitness tracking, ...).

They also impose arbitrary limits on random things – like how many times can you schedule an alarm during a given period. If you go over the limit – boom! Your code doesn't run. That’s what you get for not reading the documentation on vendor modifications (oh wait, there is *no documentation*).

How to prevent the alarm and sleep tracking from failing? **Opt out of the battery savers.**

Which may or may not be so simple.