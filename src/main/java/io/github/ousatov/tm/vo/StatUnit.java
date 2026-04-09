package io.github.ousatov.tm.vo;

/**
 * Contains statistics information
 *
 * @author Oleksii Usatov
 */
public record StatUnit(long currentCount, long totalCount, long totalErrorCount) {
    public static StatUnit EMPTY = new StatUnit(0, 0, 0);
}