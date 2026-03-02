package io.github.piscescup.linq.enumerator;

/**
 * An enumerator that produces an arithmetic progression of {@link Integer} values.
 *
 * <p>This enumerator yields exactly {@code count} integers starting from {@code start} (inclusive),
 * where each subsequent value is increased by {@code step}.</p>
 *
 * <p>The generated sequence is:</p>
 *
 * <pre>{@code
 * start, start + step, start + 2*step, ..., start + (count - 1)*step
 * }</pre>
 *
 * <h2>Execution model</h2>
 * <ul>
 *   <li><b>Lazy:</b> values are generated on demand during enumeration.</li>
 *   <li><b>Single-pass:</b> the enumerator maintains internal state.</li>
 *   <li><b>Deterministic:</b> each call to {@link #moveNextCore()} advances by one element.</li>
 * </ul>
 *
 * <h2>Edge cases</h2>
 * <ul>
 *   <li>If {@code count == 0}, the sequence is empty.</li>
 *   <li>If {@code count < 0}, an {@link IllegalArgumentException} is thrown.</li>
 *   <li>If {@code step == 0}, an {@link IllegalArgumentException} is thrown.</li>
 *   <li>Integer overflow is checked using {@link Math#addExact(int, int)} and will throw
 *       {@link ArithmeticException} during enumeration if it occurs.</li>
 * </ul>
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public class RangeEnumerator extends AbstractEnumerator<Integer> {

    private final int start;
    private final int count;
    private final int step;

    /** Number of elements already produced. */
    private int produced = 0;

    /** Next value to emit. */
    private int nextValue;

    /**
     * Constructs a new {@code RangeEnumerator}.
     *
     * @param start the first value in the range (inclusive)
     * @param count the number of elements to generate (must be non-negative)
     * @param step  the step between consecutive values (must be non-zero)
     * @throws IllegalArgumentException if {@code count < 0} or {@code step == 0}
     */
    public RangeEnumerator(int start, int count, int step) {
        if (count < 0) {
            throw new IllegalArgumentException("Count cannot be negative");
        }
        if (step == 0) {
            throw new IllegalArgumentException("Step cannot be zero");
        }

        this.start = start;
        this.count = count;
        this.step = step;
        this.nextValue = start;
    }

    public RangeEnumerator(int start, int count) {
        this(start, count, 1);
    }

    @Override
    protected boolean moveNextCore() {
        if (produced >= count) {
            return false;
        }

        // Emit current nextValue
        this.current = nextValue;
        produced++;

        // Prepare next value (only if there will be a next move)
        if (produced < count) {
            nextValue = Math.addExact(nextValue, step);
        }

        return true;
    }

    @Override
    protected AbstractEnumerator<Integer> clone() throws CloneNotSupportedException {
        RangeEnumerator e = new RangeEnumerator(start, count, step);
        e.produced = this.produced;
        e.nextValue = this.nextValue;
        // current 由 AbstractEnumerator 维护；clone 后保持一致更直观
        e.current = this.current;
        return e;
    }
}