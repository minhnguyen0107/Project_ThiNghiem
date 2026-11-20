import sys
import numpy as np
import pandas as pd
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

src = sys.argv[1] if len(sys.argv) > 1 else "results/results.csv"
outdir = src.rsplit("/", 1)[0] if "/" in src else "."

df = pd.read_csv(src)
df["time_ms"] = df["time_ns"] / 1e6

#1. Thống kê mô tả
g = df.groupby(["type", "n", "cutoff"])["time_ms"]
summary = g.agg(["count", "mean", "median", "std", "min", "max"]).reset_index()
summary["ci95"] = 1.96 * summary["std"] / np.sqrt(summary["count"])
base = summary[summary["cutoff"] == 1].set_index(["type", "n"])["mean"]
summary["ratio_vs_1"] = summary.apply(lambda r: r["mean"] / base[(r["type"], r["n"])], axis=1)
summary.to_csv(f"{outdir}/summary.csv", index=False, float_format="%.4f")

#2. CUTOFF tốt nhất
rows = []
for (t, n), grp in summary.groupby(["type", "n"]):
    best = grp.loc[grp["mean"].idxmin()]
    near = grp[grp["mean"] <= best["mean"] * 1.02]["cutoff"].tolist()
    indist = grp[(grp["mean"] - grp["ci95"]) <= (best["mean"] + best["ci95"])]["cutoff"].tolist()
    c7 = grp[grp["cutoff"] == 7]["mean"].iloc[0]
    c10 = grp[grp["cutoff"] == 10]["mean"].iloc[0]
    rows.append({
        "type": t, "n": n,
        "best_cutoff": int(best["cutoff"]),
        "best_mean_ms": best["mean"],
        "within_2pct": " ".join(map(str, near)),
        "ci_overlap_with_best": " ".join(map(str, indist)),
        "cutoff1_mean_ms": base[(t, n)],
        "speedup_vs_1": base[(t, n)] / best["mean"],
        "cutoff7_vs_best": c7 / best["mean"],
        "cutoff10_vs_best": c10 / best["mean"],
    })
best_df = pd.DataFrame(rows)
best_df.to_csv(f"{outdir}/best_cutoff.csv", index=False, float_format="%.4f")
print(best_df.to_string(index=False))

#3. Đồ thị
sizes = sorted(df["n"].unique())
for t in df["type"].unique():
    fig, axes = plt.subplots(1, len(sizes), figsize=(5 * len(sizes), 4), squeeze=False)
    for ax, n in zip(axes[0], sizes):
        s = summary[(summary["type"] == t) & (summary["n"] == n)].sort_values("cutoff")
        ax.errorbar(s["cutoff"], s["mean"], yerr=s["ci95"], fmt="o-", ms=3, capsize=2)
        ax.axvline(7, color="gray", ls="--", lw=1, label="7")
        ax.axvline(10, color="red", ls="--", lw=1, label="10")
        ax.set_xscale("log")
        ax.set_title(f"{t}, N={n:,}")
        ax.set_xlabel("CUTOFF (log)")
        ax.set_ylabel("Thời gian trung bình (ms)")
        ax.legend()
    fig.tight_layout()
    fig.savefig(f"{outdir}/plot_time_{t}.png", dpi=130)
    plt.close(fig)

    fig, ax = plt.subplots(figsize=(7, 4.5))
    for n in sizes:
        s = summary[(summary["type"] == t) & (summary["n"] == n)].sort_values("cutoff")
        ax.plot(s["cutoff"], s["ratio_vs_1"], "o-", ms=3, label=f"N={n:,}")
    ax.axhline(1.0, color="black", lw=0.8)
    ax.axvline(7, color="gray", ls="--", lw=1)
    ax.axvline(10, color="red", ls="--", lw=1)
    ax.set_xscale("log")
    ax.set_xlabel("CUTOFF (log)")
    ax.set_ylabel("Thời gian / thời gian của CUTOFF=1")
    ax.set_title(f"Tỉ lệ thời gian so với mergesort gốc ({t})")
    ax.legend()
    fig.tight_layout()
    fig.savefig(f"{outdir}/plot_ratio_{t}.png", dpi=130)
    plt.close(fig)

print("Done. Files saved in", outdir)
