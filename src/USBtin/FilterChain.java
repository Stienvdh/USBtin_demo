package USBtin;

/**
 * Represents a CAN filter chain.
 */
public class FilterChain {

    /** Filter mask */
    FilterMask mask;

    /** Filters */
    FilterValue[] filters;

    /**
     * Create filter chain with one mask and filters.
     *
     * @param mask Mask
     * @param filters Filters
     */
    public FilterChain(FilterMask mask, FilterValue[] filters) {
        this.mask = mask;
        this.filters = filters;
    }

    /**
     * Get mask of this filter chain.
     *
     * @return Mask
     */
    public FilterMask getMask() {
        return mask;
    }

    /**
     * Get filters of this filter chain.
     *
     * @return Filters
     */
    public FilterValue[] getFilters() {
        return filters;
    }

}
