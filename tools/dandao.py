import matplotlib.pyplot as plt
from matplotlib.patches import Circle


def draw_circle_family(n, v, a, b):
    fig, ax = plt.subplots(figsize=(10, 8))

    for i in range(n + 1):
        # 圆的半径与圆心纵坐标
        A = (1 - a ** i) / (1 - a) if a != 1 else i
        B = b * (1 - a ** i) / (1 - a) ** 2 - i * b / (1 - a) if a != 1 else -b * i * (i - 1) / 2

        radius = v * A

        # 绘制圆
        circle = Circle(
            (0, B),
            radius,
            fill=False,
            linewidth=1,
            alpha=0.7
        )
        ax.add_patch(circle)

        # 标记圆心
        ax.plot(0, B, ".", markersize=3)

    ax.axhline(0, color="black", linewidth=0.8)
    ax.axvline(0, color="black", linewidth=0.8)
    ax.set_aspect("equal", adjustable="datalim")
    ax.autoscale_view()
    ax.grid(True, linestyle="--", alpha=0.3)
    ax.set_xlabel("X")
    ax.set_ylabel("Y")
    ax.set_title(f"Circle Family (n={n}, v={v}, a={a}, b={b})")

    plt.show()


if __name__ == "__main__":
    n = int(input("最大 tick 数 n："))
    v = float(input("初速度 v："))
    a = float(input("阻力系数 a："))
    b = float(input("重力参数 b："))

    draw_circle_family(n, v, a, b)
