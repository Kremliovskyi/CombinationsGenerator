package com.kremlovskyi.combinationsgenerator;

/**
 * Error thrown at validation point.
 */
public class BuildError extends Error {
   public BuildError() {
   }

   BuildError(String message) {
      super(message);
   }

   public BuildError(String message, Throwable cause) {
      super(message, cause);
   }

   public BuildError(Throwable cause) {
      super(cause);
   }

   public BuildError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
      super(message, cause, enableSuppression, writableStackTrace);
   }
}
