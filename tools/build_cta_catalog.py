#!/usr/bin/env python3
"""Build the bundled CTA stop catalog shipped in app/src/main/assets/cta_stops.tsv.

CTA's BusTracker API has no geo-radius "stops near me" endpoint, so Busboy ships a local catalog
of CTA bus stops and does the radius search on-device (see CtaStopDao). CTA's static GTFS stop_id is
directly usable as a BusTracker stpid for bus stops, which are exactly the rows with
location_type == 0 AND stop_id < 30000 (the 30000+ range is rail, which the bus API rejects).

This also precomputes each stop's served routes ("lines served") via the stop_times -> trips ->
routes join so the app doesn't have to ship the giant stop_times file or make a network call.

Usage:
    python3 tools/build_cta_catalog.py <gtfs_dir> [out_tsv]

<gtfs_dir> is a directory containing an unzipped CTA google_transit feed
(https://www.transitchicago.com/downloads/sch_data/google_transit.zip). Output defaults to
app/src/main/assets/cta_stops.tsv. The TSV is tab-delimited (tabs never appear in the data) with
columns: stop_id, name, lat, lon, lines_served. Re-run to refresh when CTA revises its GTFS.
"""
import csv
import os
import sys

MAX_BUS_STOP_ID = 30000  # stop_id below this and location_type 0 == bus stop; 30000+ is rail


def natural_key(rt):
    # sort "1","2","10","X9" so numeric routes order numerically, then the rest alphabetically
    return (0, int(rt), "") if rt.isdigit() else (1, 0, rt)


def main():
    if len(sys.argv) < 2:
        sys.exit(__doc__)
    gtfs = sys.argv[1]
    here = os.path.dirname(os.path.abspath(__file__))
    out = sys.argv[2] if len(sys.argv) > 2 else os.path.join(
        here, os.pardir, "app", "src", "main", "assets", "cta_stops.tsv")

    # route_id -> short_name, buses only (route_type 3)
    route_name = {}
    with open(os.path.join(gtfs, "routes.txt"), newline="") as f:
        for r in csv.DictReader(f):
            if r["route_type"] == "3":
                route_name[r["route_id"]] = r["route_short_name"]

    # trip_id -> short_name
    trip_route = {}
    with open(os.path.join(gtfs, "trips.txt"), newline="") as f:
        for r in csv.DictReader(f):
            rt = route_name.get(r["route_id"])
            if rt is not None:
                trip_route[r["trip_id"]] = rt

    # bus stops only
    stops = {}
    with open(os.path.join(gtfs, "stops.txt"), newline="") as f:
        for r in csv.DictReader(f):
            sid = r["stop_id"]
            if r["location_type"] == "0" and sid.isdigit() and int(sid) < MAX_BUS_STOP_ID:
                stops[sid] = [r["stop_name"], r["stop_lat"], r["stop_lon"], set()]

    # stream stop_times (large) to accumulate served routes per stop; only cols 0 (trip_id) and
    # 3 (stop_id) are read, and both precede any comma-bearing field, so a naive split is safe.
    with open(os.path.join(gtfs, "stop_times.txt")) as f:
        next(f)
        for line in f:
            parts = line.split(",", 4)
            if len(parts) < 4:
                continue
            stop = stops.get(parts[3])
            rt = trip_route.get(parts[0])
            if stop is not None and rt is not None:
                stop[3].add(rt)

    os.makedirs(os.path.dirname(out), exist_ok=True)
    with open(out, "w", newline="") as f:
        for sid, (name, lat, lon, routes) in sorted(stops.items(), key=lambda kv: int(kv[0])):
            lines = ", ".join(sorted(routes, key=natural_key))
            f.write("\t".join((sid, name, lat, lon, lines)) + "\n")

    print(f"wrote {len(stops)} stops to {out}")


if __name__ == "__main__":
    main()
