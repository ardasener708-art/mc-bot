package dev.turkishmc.mcmod.module;

public final class ModuleSetting {
	private final String id;
	private final String label;
	private final Type type;
	private final int min;
	private final int max;
	private final int step;
	private Object value;

	ModuleSetting(String id, String label, Type type, Object value, int min, int max) {
		this(id, label, type, value, min, max, 1);
	}

	ModuleSetting(String id, String label, Type type, Object value, int min, int max, int step) {
		this.id = id;
		this.label = label;
		this.type = type;
		this.value = value;
		this.min = min;
		this.max = max;
		this.step = step;
	}

	public String getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	public Type getType() {
		return type;
	}

	public Object getValue() {
		return value;
	}

	public boolean getBoolean() {
		return (boolean) value;
	}

	public int getInt() {
		return (int) value;
	}

	public void setValue(Object value) {
		if (type == Type.BOOLEAN && value instanceof Boolean bool) {
			this.value = bool;
		} else if (type != Type.BOOLEAN && value instanceof Number number) {
			int next = number.intValue();
			if (type == Type.INTEGER) {
				next = Math.max(min, Math.min(max, next));
			} else {
				next &= 0xFFFFFF;
			}
			this.value = next;
		}
	}

	public void adjust(boolean forward) {
		switch (type) {
			case BOOLEAN -> value = !(boolean) value;
			case INTEGER -> {
				int next = (int) value + (forward ? step : -step);
				if (next > max) {
					next = min;
				} else if (next < min) {
					next = max;
				}
				value = next;
			}
			case COLOR -> {
				int[] colors = {
						0x55FF55, 0x33CC66, 0x00A878, 0x59D5C5,
						0x33D6FF, 0x3399FF, 0x4169E1, 0x6655FF,
						0x9955FF, 0xCC66FF, 0xFF55CC, 0xFF3388,
						0xFF5555, 0xFF3333, 0xFF7744, 0xFFAA33,
						0xFFD447, 0xFFFF66, 0xFFFFFF, 0xC8D2DC,
						0x8899AA, 0xFF8888, 0x88FFDD, 0xAADDFF
				};
				int current = (int) value;
				int index = 0;
				for (int i = 0; i < colors.length; i++) {
					if (colors[i] == current) {
						index = i;
						break;
					}
				}
				value = colors[Math.floorMod(index + (forward ? 1 : colors.length - 1), colors.length)];
			}
		}
	}

	public String getDisplayValue() {
		return switch (type) {
			case BOOLEAN -> (boolean) value ? "Açık" : "Kapalı";
			case INTEGER -> Integer.toString((int) value);
			case COLOR -> String.format("#%06X", (int) value);
		};
	}

	public enum Type {
		BOOLEAN,
		INTEGER,
		COLOR
	}
}
