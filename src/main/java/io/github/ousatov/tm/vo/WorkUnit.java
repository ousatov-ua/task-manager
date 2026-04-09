package io.github.ousatov.tm.vo;

/**
 * Unit of work
 *
 * @author Oleksii Usatov
 */
public interface WorkUnit {
  WorkUnit getLastUnit();

  String getType();
}
